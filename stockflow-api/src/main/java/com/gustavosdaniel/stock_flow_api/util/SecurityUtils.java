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

/**
 * Utility component for extracting security-related information from the
 * reactive security context.
 * <p>
 * Provides methods to retrieve the authenticated user's Keycloak ID
 * (subject) and their highest assigned role from the JWT's realm access
 * claims.
 * </p>
 */
@Component
public class SecurityUtils {

    /**
     * Retrieves the currently authenticated user's Keycloak ID (JWT subject).
     *
     * @return a {@link Mono} emitting the Keycloak ID, or an
     *         {@link AccessDeniedException} if the user is not authenticated
     */
    public Mono<String> getCurrentKeycloakId(){

        return ReactiveSecurityContextHolder.getContext()
                .map(SecurityContext::getAuthentication)
                .filter(Authentication::isAuthenticated)
                .map(Authentication::getPrincipal)
                .cast(Jwt.class)
                .map(Jwt::getSubject)
                .switchIfEmpty(Mono.error(new AccessDeniedException("Usuário não autenticado.")));
    }

    /**
     * Retrieves the highest {@link UserRole} of the currently authenticated user
     * from the JWT's {@code realm_access.roles} claim.
     * <p>
     * Roles are sorted by their level (as defined in {@link UserRole#getLevel()})
     * and the highest is returned. If no valid role is found, {@link UserRole#EMPLOYEE}
     * is the default.
     * </p>
     *
     * @return a {@link Mono} emitting the highest {@link UserRole}, defaulting
     *         to {@code EMPLOYEE} if unauthenticated or no roles match
     */
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
