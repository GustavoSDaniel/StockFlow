package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.domain.po.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface CategoryRepository extends R2dbcRepository<Category, UUID> {


    Mono<Boolean> existsByNameIgnoreCase(String name);

    Flux<Category> findAllBy(Pageable pageable);

    Flux<Category> findByIsActiveTrue(Pageable pageable);

    Mono<Long> countByIsActiveTrue();

    Flux<Category> findByParentId(UUID parentId,Pageable pageable);

    Flux<Category> findByParentIdAndIsActiveTrue(UUID parentId,Pageable pageable);

    Flux<Category> findByParentIdAndIsActiveFalse(UUID parentId,Pageable pageable);

    Mono<Long> countByParentId(UUID parentId);

    Mono<Long> countByParentIdAndIsActiveTrue(UUID parentId);

    Mono<Long> countByParentIdAndIsActiveFalse(UUID parentId);

    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Category> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Flux<Category> searchActiveByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Mono<Long> countActiveByName(String name);

    Flux<Category> findByIsActiveFalse(Pageable pageable);

    Mono<Long> countByIsActiveFalse();
}
