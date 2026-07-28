package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Category} entities.
 * <p>
 * Provides queries for searching, filtering by active status and parent
 * relationships, and counting results for pagination.
 * </p>
 */
public interface CategoryRepository extends R2dbcRepository<Category, UUID> {

    /**
     * Checks whether a category with the given name already exists (case-insensitive).
     *
     * @param name the category name to check
     * @return a {@link Mono} emitting {@code true} if a matching category exists
     */
    Mono<Boolean> existsByNameIgnoreCase(String name);

    /**
     * Finds all categories with pagination support.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link Category} entities
     */
    Flux<Category> findAllBy(Pageable pageable);

    /**
     * Finds all active categories.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of active {@link Category} entities
     */
    Flux<Category> findByActiveTrue(Pageable pageable);

    /**
     * Counts the total number of active categories.
     *
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByActiveTrue();

    /**
     * Finds all categories that are children of the given parent category.
     *
     * @param parentId the parent category's unique identifier
     * @param pageable pagination parameters
     * @return a {@link Flux} of child {@link Category} entities
     */
    Flux<Category> findByParentId(UUID parentId, Pageable pageable);

    /**
     * Finds active child categories of the given parent.
     *
     * @param parentId the parent category's unique identifier
     * @param pageable pagination parameters
     * @return a {@link Flux} of active child {@link Category} entities
     */
    Flux<Category> findByParentIdAndActiveTrue(UUID parentId, Pageable pageable);

    /**
     * Finds inactive child categories of the given parent.
     *
     * @param parentId the parent category's unique identifier
     * @param pageable pagination parameters
     * @return a {@link Flux} of inactive child {@link Category} entities
     */
    Flux<Category> findByParentIdAndActiveFalse(UUID parentId, Pageable pageable);

    /**
     * Counts child categories for the given parent.
     *
     * @param parentId the parent category's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByParentId(UUID parentId);

    /**
     * Counts active child categories for the given parent.
     *
     * @param parentId the parent category's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByParentIdAndActiveTrue(UUID parentId);

    /**
     * Counts inactive child categories for the given parent.
     *
     * @param parentId the parent category's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByParentIdAndActiveFalse(UUID parentId);

    /**
     * Searches categories by name using a case-insensitive partial match.
     *
     * @param name     the search term
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Category} entities
     */
    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Category> searchByName(String name, Pageable pageable);

    /**
     * Counts categories matching the given name (case-insensitive partial match).
     *
     * @param name the search term
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    /**
     * Searches active categories by name using a case-insensitive partial match.
     *
     * @param name     the search term
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching active {@link Category} entities
     */
    @Query("SELECT * FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Flux<Category> searchActiveByName(String name, Pageable pageable);

    /**
     * Counts active categories matching the given name (case-insensitive partial match).
     *
     * @param name the search term
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM categories WHERE name ILIKE CONCAT('%', :name, '%') AND active = true")
    Mono<Long> countActiveByName(String name);

    /**
     * Finds all inactive categories.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of inactive {@link Category} entities
     */
    Flux<Category> findByActiveFalse(Pageable pageable);

    /**
     * Counts the total number of inactive categories.
     *
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByActiveFalse();
}
