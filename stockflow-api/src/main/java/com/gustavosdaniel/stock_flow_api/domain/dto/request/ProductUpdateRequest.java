package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;

public record ProductUpdateRequest(

        String name,

        String description,

        @PositiveOrZero(message = "O custo do produto não pode ser negativo")
        BigDecimal costPrice,

        @PositiveOrZero(message = "O preço do produto não pode ser negativo")
        BigDecimal salePrice,

        UnitMeasure unitMeasure,

        @Pattern(regexp = "\\d{8}|\\d{12,14}",
                message = "Código de barras deve ter 8 (EAN-8) ou 12-14 dígitos (EAN-13/UPC)")
        String barcode
) {
    public ProductUpdateRequest {

        if (name != null) name = name.trim();
        if (description != null) description = description.trim();
        if (barcode != null) barcode = barcode.trim();
    }
}
