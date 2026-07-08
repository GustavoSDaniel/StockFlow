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

public interface ProductRepository extends R2dbcRepository<Product, UUID> {

    Mono<Boolean> existsBySku(String sku);

    Mono<Boolean> existsByNameAndStatus(String name, ProductStatus status);

    Mono<Product> findBySku(String sku);

    Flux<Product> findAllBy(Pageable pageable);

    Flux<Product> findAllByStatus(ProductStatus status, Pageable pageable);

    Mono<Long> countByStatus(ProductStatus status);

    Flux<Product> findAllByCategoryId(UUID categoryId, Pageable pageable);

    Mono<Long> countByCategoryId(UUID categoryId);

    Flux<Product> findAllBySupplierId(UUID supplierId, Pageable pageable);

    Mono<Long> countBySupplierId(UUID supplierId);

    @Query("SELECT * FROM products WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Product> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM products WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    @Query("SELECT * FROM products WHERE name ILIKE CONCAT('%', :name, '%') AND status = :status ")
    Flux<Product> searchNameAndStatus(String name, ProductStatus status, Pageable pageable);

    @Query("SELECT COUNT(*) FROM products WHERE name ILIKE CONCAT('%', :name, '%') AND status = :status ")
    Mono<Long> countNameAndStatus(String name, ProductStatus status);

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
