package com.gustavosdaniel.stock_flow_api.config;

import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ser.std.StringSerializer;
import org.apache.kafka.clients.producer.ProducerConfig;
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

    @Bean
    public KafkaSender<String, Object> kafkaSender(KafkaProperties kafkaProperties){

        Map<String, Object> producer = kafkaProperties.buildProducerProperties();

        SenderOptions<String, Object> senderOptions = SenderOptions.create(producer);

        return KafkaSender.create(senderOptions);
    }

    @Bean
    public KafkaReceiver<String, Object> kafkaReceiver(KafkaProperties kafkaProperties){

        Map<String, Object> consumer = kafkaProperties.buildConsumerProperties();

        ReceiverOptions<String, Object> receiverOptions = ReceiverOptions
                .<String, Object>create(consumer)
                .subscription(Collections.singleton("stockflow.inventory.alerts.v1"));

        return KafkaReceiver.create(receiverOptions);
    }

}
