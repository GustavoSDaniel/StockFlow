package com.gustavosdaniel.stock_flow_api.config;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

/**
 * Spring configuration for reactive notification streaming.
 * <p>
 * Provides a {@link reactor.core.publisher.Sinks.Many} multicast sink used to
 * push {@link com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse}
 * events to connected SSE (Server-Sent Events) subscribers via backpressure buffer.
 * </p>
 */
@Configuration
public class NotificationConfig {

    /**
     * Creates a multicast sink for broadcasting notification responses to SSE clients.
     * <p>
     * The sink uses a fixed backpressure buffer of 1024 queued signals. When the buffer
     * is full, new signals are dropped ({@code false} flag) rather than blocking the
     * producer.
     * </p>
     *
     * @return a many-to-many sink for {@link NotificationResponse} events
     */
    @Bean
    public Sinks.Many<NotificationResponse> notificationSinks(){

        return Sinks.many().multicast().onBackpressureBuffer(1024, false);
    }
}
