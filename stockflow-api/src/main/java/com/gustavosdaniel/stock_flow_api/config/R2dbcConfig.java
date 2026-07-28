package com.gustavosdaniel.stock_flow_api.config;

import com.gustavosdaniel.stock_flow_api.domain.enums.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

/**
 * Spring configuration for R2DBC database interaction.
 * <p>
 * Enables reactive auditing (automatic population of {@code createdBy} / {@code updatedBy}
 * fields from the JWT subject) and registers custom read/write converters that map
 * Java {@code enum} constants to PostgreSQL string columns, avoiding the need for
 * an enum type in the database schema.
 * </p>
 */
@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorAware")
public class R2dbcConfig {

    private final Logger log = LoggerFactory.getLogger(R2dbcConfig.class);

    /**
     * Provides the current authenticated user's UUID for auditing purposes.
     * <p>
     * Extracts the subject claim from the reactive security context and converts it
     * to a {@link UUID}. Returns an empty {@link Mono} when no authenticated user
     * is present or the subject is not a valid UUID.
     * </p>
     *
     * @return a reactive auditor-aware bean that resolves the current user's UUID
     */
    @Bean
    public ReactiveAuditorAware<UUID> auditorAware() {
        return () -> ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .mapNotNull(auth -> {
                    try {
                        return UUID.fromString(auth.getName());
                    } catch (IllegalArgumentException e) {
                        log.warn("JWT subject is not a valid UUID: {}", auth.getName());
                        return null;
                    }
                })
                .switchIfEmpty(Mono.empty());
    }

    /**
     * Registers custom type converters that map Java enums to/from PostgreSQL string columns.
     * <p>
     * This eliminates the need for native PostgreSQL enum types and simplifies
     * schema evolution. Converters are wired for: {@code StateUF}, {@code UnitMeasure},
     * {@code ProductStatus}, {@code MovementType}, {@code MovementReason},
     * {@code NotificationPriority}, {@code NotificationType}, and {@code UserRole}.
     * </p>
     *
     * @return an {@link R2dbcCustomConversions} instance registered against the PostgreSQL dialect
     */
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
                new MovementTypeReadConverter(),
                new NotificationPriorityWriteConverter(),
                new NotificationPriorityReadConverter(),
                new NotificationTypeWriteConverter(),
                new NotificationTypeReadConverter(),
                new UserRoleWriteConverter(),
                new UserRoleReadConverter()
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

    @WritingConverter
    static class NotificationPriorityWriteConverter implements Converter<NotificationPriority, String> {
        @Override
        public String convert(NotificationPriority reason) { return reason.name(); }
    }

    @ReadingConverter
    static class NotificationPriorityReadConverter implements Converter<String, NotificationPriority> {
        @Override
        public NotificationPriority convert(String value) { return NotificationPriority.valueOf(value); }
    }

    @WritingConverter
    static class NotificationTypeWriteConverter implements Converter<NotificationType, String> {
        @Override
        public String convert(NotificationType reason) { return reason.name(); }
    }

    @ReadingConverter
    static class NotificationTypeReadConverter implements Converter<String, NotificationType> {
        @Override
        public NotificationType convert(String value) { return NotificationType.valueOf(value); }
    }

    @WritingConverter
    static class UserRoleWriteConverter implements Converter<UserRole, String> {
        @Override
        public String convert(UserRole reason) { return reason.name(); }
    }

    @ReadingConverter
    static class UserRoleReadConverter implements Converter<String, UserRole> {
        @Override
        public UserRole convert(String value) { return UserRole.valueOf(value); }
    }

}
