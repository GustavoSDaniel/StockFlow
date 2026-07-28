package com.gustavosdaniel.stock_flow_api.config;

import com.gustavosdaniel.stock_flow_api.messaging.event.InventoryAlertEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;

import java.util.Collections;
import java.util.Map;

/**
 * Spring configuration for reactive Kafka producers and consumers.
 * <p>
 * Sets up a {@link reactor.kafka.sender.KafkaSender} for publishing
 * {@link com.gustavosdaniel.stock_flow_api.messaging.event.InventoryAlertEvent}
 * messages and a {@link reactor.kafka.receiver.KafkaReceiver} subscribed to the
 * stock-alerts topic, both leveraging Spring Boot's auto-configured
 * {@link org.springframework.boot.kafka.autoconfigure.KafkaProperties}.
 * </p>
 */
@Configuration
public class KafkaConfig {

    @Value("${inventory.kafka.topics.stock-alerts:stockflow.inventory.alerts.v1}")
    private String stockAlertsTopic;

    /**
     * Creates a reactive Kafka sender for publishing inventory-alert events.
     *
     * @param kafkaProperties Spring Boot's Kafka configuration properties
     * @return a configured {@link KafkaSender} for {@link InventoryAlertEvent} payloads
     */
    @Bean
    public KafkaSender<String, InventoryAlertEvent> kafkaSender(KafkaProperties kafkaProperties){

        Map<String, Object> producer = kafkaProperties.buildProducerProperties();

        SenderOptions<String, InventoryAlertEvent> senderOptions = SenderOptions.create(producer);

        return KafkaSender.create(senderOptions);
    }

    /**
     * Creates a reactive Kafka receiver subscribed to the stock-alerts topic.
     *
     * @param kafkaProperties Spring Boot's Kafka configuration properties
     * @return a configured {@link KafkaReceiver} for {@link InventoryAlertEvent} payloads
     */
    @Bean
    public KafkaReceiver<String, InventoryAlertEvent> kafkaReceiver(KafkaProperties kafkaProperties){

        Map<String, Object> consumer = kafkaProperties.buildConsumerProperties();

        ReceiverOptions<String, InventoryAlertEvent> receiverOptions = ReceiverOptions
                .<String, InventoryAlertEvent>create(consumer)
                .subscription(Collections.singleton(stockAlertsTopic));

        return KafkaReceiver.create(receiverOptions);
    }

}
