package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends R2dbcRepository<User, UUID> {

    Mono<User> findByKeycloakId(String keycloakId);

    @Query("SELECT * FROM users WHERE user_name ILIKE CONCAT('%', :name, '%')")
    Flux<User> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM users WHERE user_name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    Flux<User> findAllBy(Pageable pageable);
}
