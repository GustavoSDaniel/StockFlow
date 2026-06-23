package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;

import java.util.UUID;

public record StockSummaryResponse(

        UUID id,
        UUID productId,
        String productName,
        String sku,
        Integer currentQuantity,
        StockStatus status,
        String location

) {
}
