package com.gustavosdaniel.stock_flow_api.util;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
public class SecurityUtils {

    public Mono<String> getCurrentKeycloakId(){

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(Jwt::getSubject)
                .switchIfEmpty(Mono.error(new AccessDeniedException("Usuário não autenticado.")));
    }

    public Mono<UserRole> getCurrentUserRole(){

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(this::extractHighestRole)
                .switchIfEmpty(Mono.just(UserRole.EMPLOYEE));
    }

    private UserRole extractHighestRole(Jwt jwt){

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");

        if (realmAccess == null || !realmAccess.containsKey("roles")){
            return UserRole.EMPLOYEE;
        }

        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) realmAccess.get("roles");

        return roles.stream()
                .map(roleStr -> {
                    try {
                        return UserRole.valueOf(roleStr.toUpperCase());
                    }catch (IllegalArgumentException e) {
                        return null;
                    }
                } )
                .filter(java.util.Objects::nonNull)
                .max(java.util.Comparator.comparingInt(UserRole::getLevel))
                .orElse(UserRole.EMPLOYEE);
    }
}
