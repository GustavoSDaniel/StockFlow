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

@Configuration
public class KafkaConfig {

    @Value("${inventory.kafka.topics.stock-alerts:stockflow.inventory.alerts.v1}")
    private String stockAlertsTopic;

    @Bean
    public KafkaSender<String, InventoryAlertEvent> kafkaSender(KafkaProperties kafkaProperties){

        Map<String, Object> producer = kafkaProperties.buildProducerProperties();

        SenderOptions<String, InventoryAlertEvent> senderOptions = SenderOptions.create(producer);

        return KafkaSender.create(senderOptions);
    }

    @Bean
    public KafkaReceiver<String, InventoryAlertEvent> kafkaReceiver(KafkaProperties kafkaProperties){

        Map<String, Object> consumer = kafkaProperties.buildConsumerProperties();

        ReceiverOptions<String, InventoryAlertEvent> receiverOptions = ReceiverOptions
                .<String, InventoryAlertEvent>create(consumer)
                .subscription(Collections.singleton(stockAlertsTopic));

        return KafkaReceiver.create(receiverOptions);
    }

}
