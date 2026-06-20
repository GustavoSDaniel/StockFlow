package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;

import java.util.UUID;

public record StockResponse(

        UUID id,
        ProductResponse productResponse,
        Integer currentQuantity,
        Integer minimumQuantity,
        Integer maximumQuantity,
        Integer reorderPoint,
        Integer reorderQuantity,
        StockStatus status,
        String location,
        String warehouseId
) {
}
