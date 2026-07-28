package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Stock} entities.
 * <p>
 * Provides queries for finding stock by product and warehouse, identifying
 * out-of-stock, low stock, and overstock situations, and computing dashboard
 * financial stats and stock status breakdowns.
 * </p>
 */
public interface StockRepository extends R2dbcRepository<Stock, UUID> {

    /**
     * Finds a stock record by product and warehouse.
     *
     * @param productId   the product's unique identifier
     * @param warehouseId the warehouse identifier
     * @return a {@link Mono} emitting the {@link Stock}, or empty if not found
     */
    Mono<Stock> findByProductIdAndWarehouseId(UUID productId, String warehouseId);

    /**
     * Checks whether a stock record exists for the given product and warehouse.
     *
     * @param productId   the product's unique identifier
     * @param warehouseId the warehouse identifier
     * @return a {@link Mono} emitting {@code true} if the stock record exists
     */
    Mono<Boolean> existsByProductIdAndWarehouseId(UUID productId, String warehouseId);

    /**
     * Finds all stock records for the given product.
     *
     * @param productId the product's unique identifier
     * @return a {@link Flux} of {@link Stock} entities
     */
    Flux<Stock> findAllStockByProductId(UUID productId);

    /**
     * Finds stock records for the given product with pagination.
     *
     * @param productId the product's unique identifier
     * @param limit     maximum number of records to return
     * @param offset    number of records to skip
     * @return a {@link Flux} of {@link Stock} entities
     */
    @Query("SELECT * FROM stocks WHERE product_id = :productId LIMIT :limit OFFSET :offset")
    Flux<Stock> findAllStockByProductId(UUID productId, int limit, Long offset);

    /**
     * Counts stock records for the given product.
     *
     * @param productId the product's unique identifier
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(id) FROM stocks WHERE product_id = :productId")
    Mono<Long> countByProductId(UUID productId);

    /**
     * Finds all stock records with pagination.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link Stock} entities
     */
    Flux<Stock> findAllBy(Pageable pageable);

    /**
     * Finds stock records with zero current quantity (out of stock),
     * with pagination.
     *
     * @param limit  maximum number of records to return
     * @param offset number of records to skip
     * @return a {@link Flux} of out-of-stock {@link Stock} entities
     */
    @Query("SELECT * FROM stocks WHERE current_quantity = 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findOutOfStock(int limit, Long offset);

    /**
     * Counts stock records that are out of stock (current quantity is zero).
     *
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity = 0")
    Mono<Long> countOutOfStock();

    /**
     * Finds stock records with current quantity at or below the minimum quantity
     * but above zero (low stock), with pagination.
     *
     * @param limit  maximum number of records to return
     * @param offset number of records to skip
     * @return a {@link Flux} of low-stock {@link Stock} entities
     */
    @Query("SELECT * FROM stocks WHERE current_quantity <= minimum_quantity " +
            "AND current_quantity > 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findLowStock(int limit, Long offset);

    /**
     * Counts stock records that are low (current quantity at or below minimum,
     * but above zero).
     *
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity <= minimum_quantity AND current_quantity > 0")
    Mono<Long> countLowStock();

    /**
     * Finds stock records where current quantity exceeds the maximum quantity
     * (overstock), with pagination.
     *
     * @param limit  maximum number of records to return
     * @param offset number of records to skip
     * @return a {@link Flux} of over-stocked {@link Stock} entities
     */
    @Query("SELECT * FROM stocks WHERE current_quantity > maximum_quantity LIMIT :limit OFFSET :offset")
    Flux<Stock> findOverStock(int limit, Long offset);

    /**
     * Counts stock records that are overstocked (current quantity exceeds maximum).
     *
     * @return a {@link Mono} emitting the count
     */
    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity > maximum_quantity")
    Mono<Long> countOverStock();

    /**
     * Retrieves aggregate financial statistics for the dashboard: total stock value,
     * potential sales value, and average margin percentage. Only active products are
     * considered.
     *
     * @return a {@link Mono} emitting a {@link DashboardOverviewResponse.FinancialStats}
     */
    @Query("""
        SELECT
            COALESCE(SUM(s.current_quantity * p.cost_price), 0) as total_stock_value,
            COALESCE(SUM(s.current_quantity * p.sale_price), 0) as potential_sales_value,
            COALESCE(AVG((p.sale_price - p.cost_price) / NULLIF(p.sale_price, 0)) * 100) as average_margin_percentage
        FROM Products p
        JOIN Stocks s ON p.id = s.product_id
        WHERE p.status = 'ACTIVE'
        """)
    Mono<DashboardOverviewResponse.FinancialStats> getDashboardFinancialStats();

    /**
     * Retrieves stock status counts (out of stock, low stock, reorder point,
     * normal, overstocked) for the dashboard.
     *
     * @return a {@link Mono} emitting a {@link DashboardStockResponse.StockStatusCounts}
     */
    @Query("""
        SELECT
            COUNT(id) FILTER (WHERE current_quantity = 0) as out_of_stock,
            COUNT(id) FILTER (WHERE current_quantity <= minimum_quantity AND current_quantity > 0 AND current_quantity != reorder_point) as low_stock,
            COUNT(id) FILTER (WHERE current_quantity = reorder_point) as reorder_point,
            COUNT(id) FILTER (WHERE current_quantity > minimum_quantity AND current_quantity <= maximum_quantity) as normal,
            COUNT(id) FILTER (WHERE current_quantity > maximum_quantity) as over_stocked
        FROM stocks
        """)
    Mono<DashboardStockResponse.StockStatusCounts> getDashboardStockStatusCounts();

    /**
     * Retrieves the top 10 products with the lowest stock relative to their
     * minimum quantity, excluding zero-quantity items. Ordered by the ratio
     * of current to minimum quantity ascending.
     *
     * @return a {@link Flux} of {@link DashboardStockResponse.ProductStockItem}
     */
    @Query("""
        SELECT
            p.id as product_id,
            p.name as product_name,
            p.sku,
            s.current_quantity,
            s.minimum_quantity,
            CASE
                WHEN s.current_quantity = s.reorder_point THEN 'REORDER_POINT'
                WHEN s.current_quantity <= s.minimum_quantity THEN 'LOW'
                WHEN s.current_quantity > s.maximum_quantity THEN 'OVER_STOCKED'
                ELSE 'NORMAL'
            END as status
        FROM stocks s
        JOIN products p ON p.id = s.product_id
        WHERE s.current_quantity > 0
        AND s.minimum_quantity > 0
        ORDER BY (s.current_quantity::float / s.minimum_quantity) ASC
        LIMIT 10
        """)
    Flux<DashboardStockResponse.ProductStockItem> getTop10LowestStock();

    /**
     * Retrieves the top 10 products with the highest current stock quantity,
     * ordered by quantity descending.
     *
     * @return a {@link Flux} of {@link DashboardStockResponse.ProductStockItem}
     */
    @Query("""
        SELECT
            p.id as product_id,
            p.name as product_name,
            p.sku,
            s.current_quantity,
            s.minimum_quantity,
            CASE
                WHEN s.current_quantity = 0 THEN 'OUT_OF_STOCK'
                WHEN s.current_quantity = s.reorder_point THEN 'REORDER_POINT'
                WHEN s.current_quantity <= s.minimum_quantity THEN 'LOW'
                WHEN s.current_quantity > s.maximum_quantity THEN 'OVER_STOCKED'
                ELSE 'NORMAL'
            END as status
        FROM stocks s
        JOIN products p ON p.id = s.product_id
        ORDER BY s.current_quantity DESC
        LIMIT 10
        """)
    Flux<DashboardStockResponse.ProductStockItem> getTop10HighestStock();
}
