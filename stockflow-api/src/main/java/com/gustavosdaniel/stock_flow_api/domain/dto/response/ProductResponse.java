package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(

        UUID id,
        String name,
        String description,
        String sku,
        UUID categoryId,
        UUID supplierId,
        BigDecimal costPrice,
        BigDecimal salePrice,
        UnitMeasure unitMeasure,
        String barcode,
        ProductStatus status,
        BigDecimal margin
) {
}
