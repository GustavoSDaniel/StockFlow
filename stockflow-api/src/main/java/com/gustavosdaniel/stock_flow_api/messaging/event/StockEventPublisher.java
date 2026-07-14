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
 * Publicador de eventos de domínio gerados por {@link InventoryMovement}.
 * <p>
 * Suporta dois modos de publicação:
 * <ul>
 *   <li><strong>Outbox (recomendado)</strong>: persiste o evento na tabela
 *       {@code outbox_events} na mesma transação do banco, delegando a
 *       publicação ao {@code OutboxScheduler}.</li>
 *   <li><strong>Direto</strong>: publica via Kafka imediatamente usando
 *       {@link StockEventProducer} (mantido para o scheduler de outbox).</li>
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
     * Publica eventos de domínio diretamente no Kafka (modo síncrono).
     * <p>
     * <strong>Atenção:</strong> este método não deve ser chamado dentro de
     * transações de banco. Prefira {@link #writeToOutbox} para garantir
     * consistência entre banco e mensageria.
     * </p>
     *
     * @param movement movimentação de estoque com eventos de domínio
     * @return Mono vazio ao concluir
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
     * Persiste os eventos de domínio na tabela de outbox para publicação
     * assíncrona pelo {@code OutboxScheduler}.
     * <p>
     * Este método deve ser chamado dentro de um {@code @Transactional} para
     * garantir que os eventos sejam persistidos na mesma transação que a
     * operação de negócio.
     * </p>
     *
     * @param movement             movimentação de estoque com eventos de domínio
     * @param outboxEventRepository repositório da tabela de outbox
     * @param topic                tópico Kafka de destino
     * @return Flux dos eventos de outbox salvos
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
