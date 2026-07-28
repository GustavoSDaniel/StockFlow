package com.gustavosdaniel.stock_flow_api.messaging.producer;

import com.gustavosdaniel.stock_flow_api.messaging.event.InventoryAlertEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderRecord;

/**
 * Reactive Kafka producer that sends {@link InventoryAlertEvent} messages
 * to the configured stock alerts topic.
 * <p>
 * The target topic is defined by the property
 * {@code inventory.kafka.topics.stock-alerts}, defaulting to
 * {@code stockflow.inventory.alerts.v1}. The product ID is used as the
 * Kafka message key for partition routing.
 * </p>
 */
@Component
public class StockEventProducer {

    private final Logger log = LoggerFactory.getLogger(StockEventProducer.class);
    private final KafkaSender<String, InventoryAlertEvent> sender;

    @Value("${inventory.kafka.topics.stock-alerts:stockflow.inventory.alerts.v1}")
    private String topic;

    public StockEventProducer(KafkaSender<String, InventoryAlertEvent> sender) {
        this.sender = sender;
    }

    /**
     * Sends an inventory alert event to the configured Kafka topic.
     * <p>
     * The event's {@code productId} serves as the message key, and the
     * event's {@code eventId} is used as the correlation metadata for
     * tracking the send result.
     * </p>
     *
     * @param event the {@link InventoryAlertEvent} to publish
     * @return a {@link Mono} that completes when the send is acknowledged
     */
    public Mono<Void> sendInventoryAlert(InventoryAlertEvent event) {

        ProducerRecord<String, InventoryAlertEvent> producerRecord = new ProducerRecord<>(
                topic,
                event.productId().toString(),
                event
        );

        SenderRecord<String, InventoryAlertEvent, String> senderRecord = SenderRecord.create(
                producerRecord,
                event.eventId().toString()
        );

        return sender.send(Mono.just(senderRecord))
                .doOnNext(result -> log.info("Mensagem enviada com sucesso para o Kafka! Topic: {}, Tipo: {}, EventId: {}",
                        result.recordMetadata().topic(),
                        event.getClass().getSimpleName(),
                        result.correlationMetadata()))
                .doOnError(e -> log.error("Erro ao enviar mensagem para o Kafka: {}", e.getMessage()))
                .then();
    }

}
