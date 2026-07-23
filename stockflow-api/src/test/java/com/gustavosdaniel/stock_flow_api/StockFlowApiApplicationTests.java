package com.gustavosdaniel.stock_flow_api;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "keycloak.client-secret=test-secret",
        "keycloak.client.id=test-client",
        "keycloak.auth-server-url=http://localhost:6062",
        "keycloak.realm=test-realm",
        "spring.flyway.enabled=false",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration"
})
class StockFlowApiApplicationTests {

    @MockitoBean
    private Keycloak keycloak;

    @MockitoBean
    private com.gustavosdaniel.stock_flow_api.messaging.outbox.OutboxScheduler outboxScheduler;

    @MockitoBean
    private com.gustavosdaniel.stock_flow_api.messaging.consumer.NotificationConsumer notificationConsumer;

    @Test
    void contextLoads() {
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        public WebClient.Builder webClientBuilder() {
            return WebClient.builder();
        }
    }
}
