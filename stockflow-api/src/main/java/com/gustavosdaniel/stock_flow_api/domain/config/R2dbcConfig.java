package com.gustavosdaniel.stock_flow_api.domain.config;

import com.gustavosdaniel.stock_flow_api.domain.enums.*;
import org.springframework.core.convert.converter.Converter;
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
                new StateUFReadConverter(),
                new UnitMeasureWriteConverter(),
                new UnitMeasureReadConverter(),
                new ProductStatusWriteConverter(),
                new ProductStatusReadConverter(),
                new MovementReasonWriteConverter(),
                new MovementReasonReadConverter(),
                new MovementTypeWriteConverter(),
                new MovementTypeReadConverter()
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

    @WritingConverter
    static class UnitMeasureWriteConverter implements Converter<UnitMeasure, String>{

        @Override
        public String convert(UnitMeasure unitMeasure) {
            return unitMeasure.name();
        }
    }

    @ReadingConverter
    static class UnitMeasureReadConverter implements Converter<String, UnitMeasure>{


        @Override
        public UnitMeasure convert(String value) {
            return UnitMeasure.valueOf(value);
        }
    }

    @WritingConverter
    static class ProductStatusWriteConverter implements Converter<ProductStatus, String> {
        @Override
        public String convert(ProductStatus status) {
            return status.name();
        }
    }

    @ReadingConverter
    static class ProductStatusReadConverter implements Converter<String, ProductStatus> {
        @Override
        public ProductStatus convert(String value) {
            return ProductStatus.valueOf(value);
        }
    }

    @WritingConverter
    static class MovementTypeWriteConverter implements Converter<MovementType, String> {
        @Override
        public String convert(MovementType type) { return type.name(); }
    }

    @ReadingConverter
    static class MovementTypeReadConverter implements Converter<String, MovementType> {
        @Override
        public MovementType convert(String value) { return MovementType.valueOf(value); }
    }

    @WritingConverter
    static class MovementReasonWriteConverter implements Converter<MovementReason, String> {
        @Override
        public String convert(MovementReason reason) { return reason.name(); }
    }

    @ReadingConverter
    static class MovementReasonReadConverter implements Converter<String, MovementReason> {
        @Override
        public MovementReason convert(String value) { return MovementReason.valueOf(value); }
    }
    
}
