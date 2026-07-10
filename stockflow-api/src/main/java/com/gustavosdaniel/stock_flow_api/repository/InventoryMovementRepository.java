package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryMovementRepository extends R2dbcRepository<InventoryMovement, UUID> {

    Flux<InventoryMovement> findAllByStockId(UUID stockId, Pageable pageable);

    Mono<Long> countByStockId(UUID stockId);

    @Query("""
        SELECT 
            COUNT(id) FILTER (WHERE DATE(created_at) = CURRENT_DATE) as total_movements_today,
            COUNT(id) FILTER (WHERE movement_type = 'ENTRY' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)) as entries_this_month,
            COUNT(id) FILTER (WHERE movement_type = 'EXIT' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)) as exits_this_month
        FROM Inventory_movements
        """)
    Mono<DashboardMovementsResponse.MovementSummary> getDashboardMovementSummary();

    @Query("""
        SELECT 
            CAST(movement_type AS VARCHAR) as movement_type, COUNT(id) as total
        FROM inventory_movements
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY movement_type
        """)
    Flux<DashboardMovementsResponse.MovementTypeCount> getDashboardMovementTypeCount();

    @Query("""
        SELECT
            CAST(movement_reason AS VARCHAR) as movement_reason, COUNT(id) as total
        FROM inventory_movements
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY movement_reason
        """)
    Flux<DashboardMovementsResponse.MovementReasonCount> getDashboardMovementReasonCount();

    @Query("""
        SELECT 
            DATE(created_at) as date, COUNT(id) as total_movements
        FROM inventory_movements
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY DATE(created_at)
        ORDER BY date ASC
        """)
    Flux<DashboardMovementsResponse.DailyHistory> getDashboardDailyHistory();

    @Query("""
        SELECT 
            p.id as product_id,
            p.name as product_name,
            p.sku,
            SUM(im.quantity) as total_quantity_moved
        FROM inventory_movements im
        JOIN products p ON p.id = product_id
        WHERE DATE_TRUNC('month', im.created_at) = DATE_TRUNC('month', CURRENT_DATE)
        GROUP BY p.id, p.name, p.sku
        ORDER BY total_quantity_moved DESC 
        LIMIT 10
        """)
    Flux<DashboardMovementsResponse.MostMovedProduct> getDashboardMostMovedProduct();
}
