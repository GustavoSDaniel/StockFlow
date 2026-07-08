package com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard;

import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;

import java.util.List;
import java.util.UUID;

public record DashboardStockResponse(

        StockStatusCounts statusCounts,
        List<ProductStockItem> top10LowestStock,
        List<ProductStockItem> top10HighestStock

) {
    public record StockStatusCounts(

            Long outOfStock,
            Long lowStock,
            Long reorderPoint,
            Long normal,
            Long overStocked
    ){}

    public record ProductStockItem(

            UUID productId,
            String productName,
            String sku,
            Integer currentQuantity,
            Integer minimumQuantity,
            StockStatus status
    ){}

}
