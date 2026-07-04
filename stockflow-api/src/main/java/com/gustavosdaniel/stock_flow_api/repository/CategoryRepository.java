package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryRepository extends R2dbcRepository<Category, UUID> {


    Mono<Boolean> existsByNameIgnoreCase(String name);

    Flux<Category> findAllBy(Pageable pageable);

    Flux<Category> findByActiveTrue(Pageable pageable);

    Mono<Long> countByActiveTrue();

    Flux<Category> findByParentId(UUID parentId,Pageable pageable);

    Flux<Category> findByParentIdAndActiveTrue(UUID parentId,Pageable pageable);

    Flux<Category> findByParentIdAndActiveFalse(UUID parentId,Pageable pageable);

    Mono<Long> countByParentId(UUID parentId);

    Mono<Long> countByParentIdAndActiveTrue(UUID parentId);

    Mono<Long> countByParentIdAndActiveFalse(UUID parentId);

    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Category> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Flux<Category> searchActiveByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Mono<Long> countActiveByName(String name);

    Flux<Category> findByActiveFalse(Pageable pageable);

    Mono<Long> countByActiveFalse();
}
