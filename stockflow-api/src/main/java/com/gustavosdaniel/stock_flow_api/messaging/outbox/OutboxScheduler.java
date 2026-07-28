package com.gustavosdaniel.stock_flow_api.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.OutboxEvent;
import com.gustavosdaniel.stock_flow_api.messaging.event.InventoryAlertEvent;
import com.gustavosdaniel.stock_flow_api.messaging.producer.StockEventProducer;
import com.gustavosdaniel.stock_flow_api.repository.OutboxEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Scheduled process that publishes events from the outbox table to Kafka.
 * <p>
 * Runs every {@code outbox.poll-interval-ms} (default: 5000 ms) and processes
 * up to 100 pending events per cycle. Failed events have their retry counter
 * incremented; after 10 consecutive failures they are abandoned with an error
 * log entry.
 * </p>
 */
@Component
public class OutboxScheduler {

    private static final Logger log = LoggerFactory.getLogger(OutboxScheduler.class);
    private static final int MAX_RETRIES = 10;
    private static final int BATCH_SIZE = 100;

    private final OutboxEventRepository outboxEventRepository;
    private final StockEventProducer stockEventProducer;
    private final ObjectMapper objectMapper;

    @Value("${inventory.kafka.topics.stock-alerts:stockflow.inventory.alerts.v1}")
    private String stockAlertsTopic;

    public OutboxScheduler(OutboxEventRepository outboxEventRepository,
                           StockEventProducer stockEventProducer,
                           ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.stockEventProducer = stockEventProducer;
        this.objectMapper = objectMapper;
    }

    /**
     * Scheduled method that fetches pending outbox events and publishes them
     * to Kafka with controlled concurrency (4 parallel publications).
     *
     * @return a {@link Mono} that completes when the cycle finishes
     */
    @Scheduled(fixedDelayString = "${outbox.poll-interval-ms:5000}")
    @Transactional
    public Mono<Void> processOutbox() {

        return outboxEventRepository.findPendingEvents(BATCH_SIZE)
                .flatMap(this::publishEvent, 4) // concorrência controlada: 4 publicações paralelas
                .doOnError(e -> log.error("Erro crítico no ciclo de outbox: {}", e.getMessage(), e))
                .doOnComplete(() -> log.debug("Ciclo de outbox concluído."))
                .then();
    }

    /**
     * Publishes a single outbox event to Kafka and updates its status.
     * <p>
     * Deserializes the stored JSON payload back into an
     * {@link InventoryAlertEvent}, sends it via
     * {@link StockEventProducer#sendInventoryAlert}, and marks the outbox
     * entry as processed or failed accordingly.
     * </p>
     *
     * @param outboxEvent the pending outbox event
     * @return a {@link Mono} that completes when the event is processed
     */
    private Mono<Void> publishEvent(OutboxEvent outboxEvent) {

        InventoryAlertEvent alertEvent;
        try {
            alertEvent = objectMapper.readValue(outboxEvent.getPayload(), InventoryAlertEvent.class);
        } catch (IOException e) {
            log.error("Falha ao desserializar evento da outbox. id={}, payload={}",
                    outboxEvent.getId(), outboxEvent.getPayload(), e);
            return markAsFailed(outboxEvent, "Desserialização falhou: " + e.getMessage());
        }

        return stockEventProducer.sendInventoryAlert(alertEvent)
                .then(Mono.defer(() -> markAsProcessed(outboxEvent)))
                .onErrorResume(e -> markAsFailed(outboxEvent,
                        e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName()))
                .then();
    }

    private Mono<Void> markAsProcessed(OutboxEvent outboxEvent) {
        outboxEvent.markAsProcessed();

        return outboxEventRepository.save(outboxEvent)
                .doOnSuccess(saved -> log.info("Evento de outbox publicado com sucesso. id={}, type={}",
                        saved.getId(), saved.getEventType()))
                .doOnError(e -> log.error("Falha ao marcar outbox como processado. id={}",
                        outboxEvent.getId(), e))
                .then();
    }

    private Mono<Void> markAsFailed(OutboxEvent outboxEvent, String errorMessage) {
        outboxEvent.markAsFailed(errorMessage);

        if (outboxEvent.getRetryCount() >= MAX_RETRIES) {
            log.error("Evento de outbox excedeu máximo de tentativas ({}). Abandonando. id={}, type={}, lastError={}",
                    MAX_RETRIES, outboxEvent.getId(), outboxEvent.getEventType(), errorMessage);
            outboxEvent.abandon();
        }

        return outboxEventRepository.save(outboxEvent)
                .doOnSuccess(saved -> log.warn("Tentativa {} falhou para outbox id={}. Erro: {}",
                        saved.getRetryCount(), saved.getId(), errorMessage))
                .then();
    }
}
