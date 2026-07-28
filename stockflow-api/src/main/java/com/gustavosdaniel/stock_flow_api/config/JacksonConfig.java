package com.gustavosdaniel.stock_flow_api.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Spring configuration for the Jackson {@link ObjectMapper}.
 * <p>
 * Registers the {@link com.fasterxml.jackson.datatype.jsr310.JavaTimeModule} and disables
 * timestamp-based date serialization so that Java 8 date/time types are serialized as
 * ISO-8601 strings. The resulting mapper is marked as {@link Primary} to serve as the
 * application-wide default.
 * </p>
 */
@Configuration
public class JacksonConfig {

    /**
     * Creates the primary application-wide {@link ObjectMapper}.
     *
     * @return a configured ObjectMapper with Java 8 date/time support
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {

        ObjectMapper mapper = new ObjectMapper();

        mapper.registerModule(new JavaTimeModule());

        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        return mapper;
    }
}
