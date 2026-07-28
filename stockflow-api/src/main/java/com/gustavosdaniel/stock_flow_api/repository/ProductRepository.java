package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Product} entities.
 * <p>
 * Provides queries for searching by name, filtering by status, category, and
 * supplier, as well as dashboard product statistics.
 * </p>
 */
public interface ProductRepository extends R2dbcRepository<Product, UUID> {

    /**
     * Checks whether a product with the given SKU already exists.
     *
     * @param sku the SKU to check
     * @return a {@link Mono} emitting {@code true} if a matching product exists
     */
    Mono<Boolean> existsBySku(String sku);

    /**
     * Checks whether a product with the given name and status exists.
     *
     * @param name   the product name
     * @param status the {@link ProductStatus}
     * @return a {@link Mono} emitting {@code true} if a matching product exists
     */
    Mono<Boolean> existsByNameAndStatus(String name, ProductStatus status);

    /**
     * Finds a single product by its SKU.
     *
     * @param sku the product SKU
     * @return a {@link Mono} emitting the {@link Product}, or empty if not found
     */
    Mono<Product> findBySku(String sku);

    /**
     * Finds all products with pagination.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link Product} entities
     */
    Flux<Product> findAllBy(Pageable pageable);

    /**
     * Finds all products with the given status.
     *
     * @param status   the {@link ProductStatus} to filter by
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Product} entities
     */
    Flux<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    /**
     * Counts products with the given status.
     *
     * @param status the {@link ProductStatus} to filter by
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByStatus(ProductStatus status);

    /**
     * Finds all products belonging to the given category.
     *
     * @param categoryId the category's unique identifier
     * @param pageable   pagination parameters
     * @return a {@link Flux} of matching {@link Product} entities
     */
    Flux<Product> findAllByCategoryId(UUID categoryId, Pageable pageable);

    /**
     * Counts products belonging to the given category.
     *
     * @param categoryId the category's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByCategoryId(UUID categoryId);

    /**
     * Finds all products belonging to the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @param pageable   pagination parameters
     * @return a {@link Flux} of matching {@link Product} entities
     */
    Flux<Product> findAllBySupplierId(UUID supplierId, Pageable pageable);

    /**
     * Counts products belonging to the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countBySupplierId(UUID supplierId);

    /**
     * Searches products by name using a case-insensitive partial match.
     *
     * @param name     the search term
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Product} entities
     */
    @Query("SELECT * FROM products WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Product> searchByName(String name, Pageable pageable);

    /**
     * Counts products matching the given name (case-insensitive partial match).
     *
     * @param name the search term
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM products WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    /**
     * Searches products by name and status using case-insensitive partial match.
     *
     * @param name     the search term
     * @param status   the {@link ProductStatus} to filter by
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Product} entities
     */
    @Query("SELECT * FROM products WHERE name ILIKE CONCAT('%', :name, '%') AND status = :status ")
    Flux<Product> searchNameAndStatus(String name, ProductStatus status, Pageable pageable);

    /**
     * Counts products matching the given name and status.
     *
     * @param name   the search term
     * @param status the {@link ProductStatus} to filter by
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(*) FROM products WHERE name ILIKE CONCAT('%', :name, '%') AND status = :status ")
    Mono<Long> countNameAndStatus(String name, ProductStatus status);

    /**
     * Retrieves aggregate product statistics (total, active, inactive, discontinued)
     * for the dashboard.
     *
     * @return a {@link Mono} emitting a {@link DashboardOverviewResponse.ProductStats}
     */
    @Query("""
        SELECT
            COUNT(id) as total,
            COUNT(id) FILTER (WHERE status = 'ACTIVE') as active,
            COUNT(id) FILTER (WHERE status = 'INACTIVE') as inactive,
            COUNT(id) FILTER (WHERE status = 'DISCONTINUED') as discontinued
        FROM products
    """)
    Mono<DashboardOverviewResponse.ProductStats> getDashboardProductStats();

}
