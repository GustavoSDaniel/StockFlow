package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link User} entities.
 * <p>
 * Provides queries for looking up users by Keycloak ID and searching by username.
 * </p>
 */
public interface UserRepository extends R2dbcRepository<User, UUID> {

    /**
     * Finds a user by their Keycloak unique identifier.
     *
     * @param keycloakId the Keycloak user ID
     * @return a {@link Mono} emitting the {@link User}, or empty if not found
     */
    Mono<User> findByKeycloakId(String keycloakId);

    /**
     * Searches users by username using a case-insensitive partial match.
     *
     * @param name     the search term
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link User} entities
     */
    @Query("SELECT * FROM users WHERE user_name ILIKE CONCAT('%', :name, '%')")
    Flux<User> searchByName(String name, Pageable pageable);

    /**
     * Counts users matching the given username (case-insensitive partial match).
     *
     * @param name the search term
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM users WHERE user_name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    /**
     * Finds all users with pagination.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link User} entities
     */
    Flux<User> findAllBy(Pageable pageable);
}
