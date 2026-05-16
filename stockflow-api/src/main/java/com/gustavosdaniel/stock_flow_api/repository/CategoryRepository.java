package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryRepository extends R2dbcRepository<Category, UUID> {


    Mono<Boolean> existsByNameIgnoreCase(String name);

    Flux<Category> findAllBy(Pageable pageable);

    Flux<Category> findByIsActiveTrue(Pageable pageable);

    Mono<Long> countByIsActiveTrue();
}
