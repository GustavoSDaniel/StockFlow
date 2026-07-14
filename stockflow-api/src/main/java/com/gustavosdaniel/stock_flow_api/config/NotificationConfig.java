package com.gustavosdaniel.stock_flow_api.config;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class NotificationConfig {

    @Bean
    public Sinks.Many<NotificationResponse> notificationSinks(){

        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}
