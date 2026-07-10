package com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record DashboardMovementsResponse(

        MovementSummary summary,
        List<MovementTypeCount> movementsByType,
        List<MovementReasonCount> movementsByReason,
        List<DailyHistory> dailyHistories,
        List<MostMovedProduct> topMovedProducts
) {

    public record MovementSummary(
            Long totalMovementsToday,
            Long entriesThisMonth,
            Long exitsThisMonth
    ) {}

    public record MovementTypeCount(
            String movementType,
            Long total
    ) {}

    public record MovementReasonCount(
            String movementReason,
            Long total
    ) {}

    public record DailyHistory(
            LocalDate date,
            Long totalMovements
    ) {}

    public record MostMovedProduct(
            UUID productId,
            String productName,
            String sku,
            Long totalQuantityMoved
    ) {}
}
