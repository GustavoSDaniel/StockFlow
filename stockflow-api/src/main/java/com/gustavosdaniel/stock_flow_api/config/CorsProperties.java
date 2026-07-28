package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Propriedades de CORS carregadas via {@code @ConfigurationProperties}.
 * <p>
 * Evita o uso de {@code @Value} com URLs que contêm {@code :}
 * (ex: {@code https://...}), cujo parsing pode truncar o valor
 * no placeholder {@code ${...:default}} do Spring.
 * </p>
 *
 * <h3>Sobrescrita via variável de ambiente (produção)</h3>
 * O Spring Boot converte automaticamente string separada por vírgula em {@code List<String>}:
 * <pre>{@code
 *   API_CORS_ALLOWED_ORIGINS=http://localhost:4200,https://stockflow.gustavosdaniel.com
 * }</pre>
 */
@Component
@ConfigurationProperties(prefix = "api.cors")
public class CorsProperties {

    /**
     * Lista de origens permitidas para CORS.
     * No application.yaml é definida como lista YAML nativa (sem risco de truncamento).
     */
    private List<String> allowedOrigins = List.of("http://localhost:4200");

    /**
     * Tempo máximo (em segundos) que o navegador pode cachear a resposta de preflight.
     */
    private long maxAge = 3600L;

    public List<String> getAllowedOrigins() {
        return allowedOrigins;
    }

    public void setAllowedOrigins(List<String> allowedOrigins) {
        this.allowedOrigins = allowedOrigins;
    }

    public long getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(long maxAge) {
        this.maxAge = maxAge;
    }
}
