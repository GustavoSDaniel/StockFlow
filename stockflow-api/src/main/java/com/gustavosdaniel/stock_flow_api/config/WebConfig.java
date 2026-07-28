package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.web.ReactivePageableHandlerMethodArgumentResolver;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.result.method.annotation.ArgumentResolverConfigurer;

/**
 * Spring WebFlux configuration supplement.
 * <p>
 * Registers a {@link ReactivePageableHandlerMethodArgumentResolver} so that
 * controller methods can accept {@link org.springframework.data.domain.Pageable}
 * parameters directly, parsed from reactive request query strings.
 * </p>
 */
@Configuration
public class WebConfig implements WebFluxConfigurer {

    /**
     * Adds a reactive pageable argument resolver to Spring WebFlux's handler chain.
     *
     * @param configurer the argument resolver configurer
     */
    @Override
    public void configureArgumentResolvers(ArgumentResolverConfigurer configurer) {
        configurer.addCustomResolver(new ReactivePageableHandlerMethodArgumentResolver());
    }
}
