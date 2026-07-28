package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.reactive.CorsWebFilter;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Spring Security configuration for WebFlux.
 * <p>
 * Sets up:
 * <ul>
 *   <li>A global {@link CorsWebFilter} that intercepts preflight (OPTIONS) requests
 *       before the OAuth2 authentication filter chain.</li>
 *   <li>A {@link SecurityWebFilterChain} securing API endpoints with role-based
 *       authorization and JWT-based resource-server authentication via Keycloak.</li>
 *   <li>A {@link ReactiveJwtAuthenticationConverter} that maps Keycloak realm and
 *       client roles to Spring Security {@code GrantedAuthority} instances.</li>
 * </ul>
 * </p>
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private final CorsProperties corsProperties;

    public SecurityConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }

    public static final String[] PUBLIC_URLS = {

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/actuator/health",
            "/actuator/health/**",
            "/actuator/prometheus"
    };

    /**
     * Configures the reactive security filter chain.
     * <p>
     * Disables CSRF, enables CORS, defines role-based authorization rules for all
     * API endpoints, and wires OAuth2 JWT resource-server authentication so that
     * Keycloak-issued tokens are automatically validated.
     * </p>
     *
     * @param http the server HTTP security builder
     * @return the built {@link SecurityWebFilterChain}
     */
    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http){

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeExchange(auth -> auth

                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(PUBLIC_URLS).permitAll()

                        //ERROR
                        .pathMatchers(HttpMethod.GET, "/api/v1/errors/**").authenticated()

                        //USER
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/me").authenticated()
                        .pathMatchers(HttpMethod.GET, "/api/v1/users/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.GET,"/api/v1/users/search/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/users/*/promote")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/users/*/active")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/users/*/disable")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/users/*").hasRole("ADMIN")

                        //CATEGORY
                        .pathMatchers(HttpMethod.GET, "/api/v1/categories/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/categories")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/categories/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/categories/*/activate")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/categories/*/disable")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/categories/**").hasRole("ADMIN")

                        //SUPPLIER
                        .pathMatchers(HttpMethod.GET, "/api/v1/suppliers/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/suppliers/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/suppliers/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/suppliers/**").hasRole("ADMIN")

                        //PRODUCT
                        .pathMatchers(HttpMethod.GET, "/api/v1/products/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/products/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/products/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PATCH, "/api/v1/products/**")
                        .hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")

                        //STOCK
                        .pathMatchers(HttpMethod.GET, "/api/v1/stocks/**").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/stocks/*/entry").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/stocks/*/exit").authenticated()
                        .pathMatchers(HttpMethod.POST, "/api/v1/stocks/**").hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/api/v1/stocks/**").hasAnyRole("MANAGER", "ADMIN")

                        // NOTIFICATIONS
                        .pathMatchers("/api/v1/notifications/**").hasAnyRole("MANAGER", "ADMIN")

                        // DASHBOARD
                        .pathMatchers("/api/v1/dashboards/stocks").authenticated()
                        .pathMatchers("/api/v1/dashboards/movements").authenticated()
                        .pathMatchers("/api/v1/dashboards/suppliers").hasAnyRole("MANAGER", "ADMIN")
                        .pathMatchers("/api/v1/dashboards/overview").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()))
                )
        .build();
    }

    /**
     * Builds a JWT authentication converter that extracts roles from Keycloak tokens.
     * <p>
     * Reads roles from both {@code realm_access.roles} and
     * {@code resource_access.stock-flow-app.roles} claims, prefixing each with
     * {@code ROLE_} to comply with Spring Security's authority naming convention.
     * </p>
     *
     * @return a reactive converter that produces {@link GrantedAuthority} instances from JWT claims
     */
    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter(){

        ReactiveJwtAuthenticationConverter converter =
                new ReactiveJwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt -> {

            List<GrantedAuthority> authorities = new ArrayList<>();

            Map<String, Object> realmAccess = jwt.getClaim("realm_access");

            if (realmAccess != null && realmAccess.containsKey("roles")){

                List<String> roles = (List<String>) realmAccess.get("roles");

                authorities.addAll(
                        roles.stream()
                                .map(role -> new SimpleGrantedAuthority(
                                        "ROLE_" + role.toUpperCase()))
                                .toList()
                );
            }

            Map<String, Object> resourceAccess = jwt.getClaim("resource_access");

            if (resourceAccess != null){

                Map<String, Object> client = (Map<String, Object>) resourceAccess
                        .get("stock-flow-app");

                if (client != null && client.containsKey("roles")){

                    List<String> roles = (List<String>) client.get("roles");

                    authorities.addAll(
                            roles.stream()
                                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
                                    .toList()
                    );
                }
            }

            return Flux.fromIterable(authorities);
        });

        return converter;
    }

    /**
     * Bean de CORS que roda como WebFilter GLOBAL, ANTES da cadeia de segurança do Spring Security.
     * Isso garante que requisições OPTIONS (preflight) sejam respondidas diretamente
     * com os headers CORS, sem jamais passar pelo OAuth2 AuthenticationWebFilter.
     */
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of(
                HttpHeaders.AUTHORIZATION,
                HttpHeaders.CONTENT_TYPE,
                HttpHeaders.CONTENT_DISPOSITION));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return new CorsWebFilter(source);
    }

    /**
     * Mantido para compatibilidade com a DSL .cors() dentro da SecurityWebFilterChain,
     * garantindo que o Spring Security também conheça a configuração de CORS.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(corsProperties.getAllowedOrigins());
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(corsProperties.getMaxAge());

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
