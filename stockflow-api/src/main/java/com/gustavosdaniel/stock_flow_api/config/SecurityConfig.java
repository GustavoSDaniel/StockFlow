package com.gustavosdaniel.stock_flow_api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    public static final String[] PUBLIC_URLS = {

            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html",
            "/swagger-resources/**",
            "/webjars/**",
            "/errors/**"
    };

    @Bean
    public SecurityWebFilterChain filterChain(ServerHttpSecurity http){

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)

                .authorizeExchange(auth -> auth
                        .pathMatchers(PUBLIC_URLS).permitAll()

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

                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(
                                jwtAuthenticationConverter()))
                )
        .build();
    }

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
}
