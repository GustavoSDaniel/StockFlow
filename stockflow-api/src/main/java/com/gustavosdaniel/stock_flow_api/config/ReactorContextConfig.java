package com.gustavosdaniel.stock_flow_api.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Hooks;

/**
 * Spring configuration for Reactor context propagation.
 * <p>
 * Enables automatic propagation of the reactive context (e.g. security context,
 * tracing spans) across thread boundaries in the entire application. This is
 * essential for correct operation of R2DBC auditing and logging MDC when
 * using reactive programming with Project Reactor.
 * </p>
 */
@Configuration
public class ReactorContextConfig {

    /**
     * Enables automatic reactive context propagation on application startup.
     */
    @PostConstruct
    public void init(){

        Hooks.enableAutomaticContextPropagation();
    }
}
