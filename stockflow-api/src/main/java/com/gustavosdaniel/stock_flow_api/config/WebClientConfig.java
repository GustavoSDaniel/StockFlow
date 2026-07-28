package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Spring configuration for reactive HTTP clients.
 * <p>
 * Exposes a {@link org.springframework.web.reactive.function.client.WebClient.Builder}
 * bean that other components can inject and customize with service-specific base URLs,
 * timeouts, and filters.
 * </p>
 */
@Configuration
public class WebClientConfig {

    /**
     * Provides a pre-configured {@link WebClient.Builder} for making reactive HTTP calls.
     *
     * @return a new WebClient builder instance
     */
    @Bean
    public WebClient.Builder webClientBuilder(){

        return WebClient.builder();

    }
}
