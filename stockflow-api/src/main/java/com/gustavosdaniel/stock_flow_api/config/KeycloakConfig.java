package com.gustavosdaniel.stock_flow_api.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the Keycloak Admin Client.
 * <p>
 * Builds a {@link org.keycloak.admin.client.Keycloak} instance using the
 * client-credentials grant type, allowing server-to-server communication
 * with the Keycloak realm for user management operations.
 * </p>
 */
@Configuration
public class KeycloakConfig {

    @Value("${KEYCLOAK_URL:http://localhost:6062}")
    private String serverUrl;

    @Value("${keycloak.realm:stock-flow-realm}")
    private String realm;

    @Value("${keycloak.client.id:stock-flow-api}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    /**
     * Builds the Keycloak admin client for programmatic realm administration.
     *
     * @return a configured {@link Keycloak} instance using client-credentials grant
     */
    @Bean
    public Keycloak keycloakAdmin(){
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
