package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import com.gustavosdaniel.stock_flow_api.domain.po.OutboxEvent;
import com.gustavosdaniel.stock_flow_api.messaging.producer.StockEventProducer;
import com.gustavosdaniel.stock_flow_api.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Publishes domain events raised by {@link InventoryMovement} entities.
 * <p>
 * Supports two publication modes:
 * <ul>
 *   <li><strong>Outbox (recommended)</strong>: persists the event into the
 *       {@code outbox_events} table within the same database transaction,
 *       delegating the actual Kafka publication to the
 *       {@code OutboxScheduler}.</li>
 *   <li><strong>Direct</strong>: publishes to Kafka immediately via
 *       {@link StockEventProducer} (used by the outbox scheduler itself).</li>
 * </ul>
 * </p>
 */
@Component
public class StockEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);
    private final StockEventProducer stockEventProducer;
    private final ObjectMapper objectMapper;

    public StockEventPublisher(StockEventProducer stockEventProducer, ObjectMapper objectMapper) {
        this.stockEventProducer = stockEventProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Publishes domain events directly to Kafka (synchronous mode).
     * <p>
     * <strong>Warning:</strong> this method must not be called inside database
     * transactions. Prefer {@link #writeToOutbox} to guarantee consistency
     * between the database and the messaging layer.
     * </p>
     *
     * @param movement the inventory movement carrying domain events
     * @return empty {@link Mono} on completion
     */
    public Mono<Void> publish(InventoryMovement movement) {

        if (movement == null || movement.getDomainEvents().isEmpty()) return Mono.empty();

        return Flux.fromIterable(movement.getDomainEvents())
                .flatMap(event -> {
                    if (event instanceof InventoryAlertEvent alertEvent) {

                        log.info("Preparando envio para o Kafka: {}  productId={}",
                                alertEvent.getClass().getSimpleName(), alertEvent.productId());

                        return stockEventProducer.sendInventoryAlert(alertEvent);
                    } else {

                        log.info("DomainEvent ignorado (sem integração Kafka) type={}, eventId={}",
                                event.getClass().getSimpleName(), event.eventId());
                        return Mono.empty();
                    }
                })
                .then();
    }

    /**
     * Persists domain events into the outbox table for asynchronous
     * publication by the {@code OutboxScheduler}.
     * <p>
     * This method should be called inside a {@code @Transactional} scope to
     * ensure events are persisted in the same transaction as the business
     * operation.
     * </p>
     *
     * @param movement              the inventory movement carrying domain events
     * @param outboxEventRepository the outbox event repository
     * @param topic                 the target Kafka topic
     * @return a {@link Flux} of saved {@link OutboxEvent} entities
     */
    public Flux<OutboxEvent> writeToOutbox(InventoryMovement movement,
                                           OutboxEventRepository outboxEventRepository,
                                           String topic) {

        if (movement == null || movement.getDomainEvents().isEmpty()) return Flux.empty();

        return Flux.fromIterable(movement.getDomainEvents())
                .filter(event -> event instanceof InventoryAlertEvent)
                .map(event -> (InventoryAlertEvent) event)
                .map(alertEvent -> {
                    try {
                        String payload = objectMapper.writeValueAsString(alertEvent);

                        return new OutboxEvent(
                                alertEvent.productId(),
                                alertEvent.getClass().getSimpleName(),
                                payload,
                                topic,
                                alertEvent.productId().toString()
                        );
                    } catch (JsonProcessingException e) {
                        log.error("Falha ao serializar evento para outbox. eventId={}, productId={}",
                                alertEvent.eventId(), alertEvent.productId(), e);
                        throw new RuntimeException("Falha ao serializar evento de domínio para JSON", e);
                    }
                })
                .flatMap(outboxEventRepository::save)
                .doOnNext(saved ->
                        log.debug("Evento persistido na outbox: id={}, type={}, topic={}",
                                saved.getId(), saved.getEventType(), saved.getTopic()));
    }
}
