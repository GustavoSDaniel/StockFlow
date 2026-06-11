package com.gustavosdaniel.stock_flow_api;

import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootTest(properties = {
    "keycloak.client-secret=test-secret",
    "keycloak.client.id=test-client",
    "keycloak.auth-server-url=http://localhost:5052",
    "keycloak.realm=test-realm"
})
class StockFlowApiApplicationTests {

    @MockitoBean
    private Keycloak keycloak;

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
