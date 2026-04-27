package com.gustavosdaniel.stock_flow_api.domain.config;

import org.springframework.core.convert.converter.Converter;
import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.convert.R2dbcCustomConversions;
import org.springframework.data.r2dbc.dialect.PostgresDialect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware")
public class R2dbcConfig {

    @Bean
    public ReactiveAuditorAware<UUID> auditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(auth -> UUID.fromString(auth.getName()))
                .switchIfEmpty(Mono.empty());
    }

    @Bean
    public R2dbcCustomConversions r2dbcCustomConversions(){

        List<Converter<?, ?>> converters = List.of(

                new StateUFWriteConverter(),
                new StateUFReadConverter()
        );
        return R2dbcCustomConversions.of(PostgresDialect.INSTANCE, converters);
    }

    @WritingConverter
    static class StateUFWriteConverter implements Converter<StateUF, String>{

        @Override
        public String convert(StateUF stateUF) {
            return stateUF.name();
        }
    }

    @ReadingConverter
    static class StateUFReadConverter implements Converter<String, StateUF>{

        @Override
        public StateUF convert(String value) {
            return StateUF.valueOf(value);
        }

    }
}
