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

public interface StockRepository extends R2dbcRepository<Stock, UUID> {

    Mono<Stock> findByProductIdAndWarehouseId(UUID productId, String warehouseId);

    Mono<Boolean> existsByProductIdAndWarehouseId(UUID productId, String warehouseId);

    Flux<Stock> findAllStockByProductId(UUID productId);

    @Query("SELECT * FROM stocks WHERE product_id = :productId LIMIT :limit OFFSET :offset")
    Flux<Stock> findAllStockByProductId(UUID productId, int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE product_id = :productId")
    Mono<Long> countByProductId(UUID productId);

    Flux<Stock> findAllBy(Pageable pageable);

    @Query("SELECT * FROM stocks WHERE current_quantity = 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findOutOfStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity = 0")
    Mono<Long> countOutOfStock();

    @Query("SELECT * FROM stocks WHERE current_quantity <= minimum_quantity " +
            "AND current_quantity > 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findLowStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity <= minimum_quantity AND current_quantity > 0")
    Mono<Long> countLowStock();

    @Query("SELECT * FROM stocks WHERE current_quantity > maximum_quantity LIMIT :limit OFFSET :offset")
    Flux<Stock> findOverStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity > maximum_quantity")
    Mono<Long> countOverStock();

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
