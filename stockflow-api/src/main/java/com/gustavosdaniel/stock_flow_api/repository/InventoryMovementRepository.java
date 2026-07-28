package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link InventoryMovement} entities.
 * <p>
 * Provides queries for listing movements by stock and computing dashboard
 * statistics such as movement summaries, type/reason breakdowns, daily history,
 * and top moved products.
 * </p>
 */
public interface InventoryMovementRepository extends R2dbcRepository<InventoryMovement, UUID> {

    /**
     * Finds all inventory movements for the given stock, with pagination.
     *
     * @param stockId  the stock's unique identifier
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link InventoryMovement} entities
     */
    Flux<InventoryMovement> findAllByStockId(UUID stockId, Pageable pageable);

    /**
     * Counts the total number of movements for the given stock.
     *
     * @param stockId the stock's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByStockId(UUID stockId);

    /**
     * Retrieves an aggregate summary of today's movements and this month's
     * entries/exits for the dashboard.
     *
     * @return a {@link Mono} emitting a {@link DashboardMovementsResponse.MovementSummary}
     */
    @Query("""
        SELECT
            COUNT(id) FILTER (WHERE DATE(created_at) = CURRENT_DATE) as total_movements_today,
            COUNT(id) FILTER (WHERE movement_type = 'ENTRY' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)) as entries_this_month,
            COUNT(id) FILTER (WHERE movement_type = 'EXIT' AND DATE_TRUNC('month', created_at) = DATE_TRUNC('month', CURRENT_DATE)) as exits_this_month
        FROM inventory_movement
        """)
    Mono<DashboardMovementsResponse.MovementSummary> getDashboardMovementSummary();

    /**
     * Counts movements grouped by type (ENTRY/EXIT) over the last 30 days.
     *
     * @return a {@link Flux} of {@link DashboardMovementsResponse.MovementTypeCount}
     */
    @Query("""
        SELECT
            CAST(movement_type AS VARCHAR) as movement_type, COUNT(id) as total
        FROM inventory_movement
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY movement_type
        """)
    Flux<DashboardMovementsResponse.MovementTypeCount> getDashboardMovementTypeCount();

    /**
     * Counts movements grouped by reason over the last 30 days.
     *
     * @return a {@link Flux} of {@link DashboardMovementsResponse.MovementReasonCount}
     */
    @Query("""
        SELECT
            CAST(reason AS VARCHAR) as movement_reason, COUNT(id) as total
        FROM inventory_movement
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY reason
        """)
    Flux<DashboardMovementsResponse.MovementReasonCount> getDashboardMovementReasonCount();

    /**
     * Retrieves daily movement counts for the last 30 days, ordered by date ascending.
     *
     * @return a {@link Flux} of {@link DashboardMovementsResponse.DailyHistory}
     */
    @Query("""
        SELECT
            DATE(created_at) as date, COUNT(id) as total_movements
        FROM inventory_movement
        WHERE created_at >= CURRENT_DATE - INTERVAL '30 days'
        GROUP BY DATE(created_at)
        ORDER BY date ASC
        """)
    Flux<DashboardMovementsResponse.DailyHistory> getDashboardDailyHistory();

    /**
     * Retrieves the top 10 most moved products for the current month,
     * ranked by total quantity moved in descending order.
     *
     * @return a {@link Flux} of {@link DashboardMovementsResponse.MostMovedProduct}
     */
    @Query("""
        SELECT
            p.id as product_id,
            p.name as product_name,
            p.sku,
            SUM(im.quantity) as total_quantity_moved
        FROM inventory_movement im
        JOIN products p ON p.id = im.product_id
        WHERE DATE_TRUNC('month', im.created_at) = DATE_TRUNC('month', CURRENT_DATE)
        GROUP BY p.id, p.name, p.sku
        ORDER BY total_quantity_moved DESC
        LIMIT 10
        """)
    Flux<DashboardMovementsResponse.MostMovedProduct> getDashboardMostMovedProduct();
}