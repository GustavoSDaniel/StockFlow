package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductRequest(

        @NotBlank(message = "O nome é obrigatório")
        String name,

        String description,

        @NotNull(message = "A categoria é obrigatória")
        UUID categoryId,

        @NotNull(message = "O fornecedor é obrigatório")
        UUID supplierId,

        @NotNull(message = "O valor de custo do produto é obrigatório")
        @PositiveOrZero(message = "O custo do produto não pode ser negativo")
        BigDecimal costPrice,

        @NotNull(message = "O valor da venda produto é obrigatório")
        @PositiveOrZero(message = "O preço do produto não pode ser negativo")
        BigDecimal salePrice,

        @NotNull(message = "A unidade de medida é obrigatório")
        UnitMeasure unitMeasure,

        @Pattern(regexp = "^$|\\d{8}|\\d{12,14}",
                message = "Código de barras deve ter 8 (EAN-8) ou 12-14 dígitos (EAN-13/UPC)")
        String barcode
) {
    public ProductRequest{

        if (name != null) name = name.trim();
        if (description != null) description = description.trim();
        if (barcode != null) barcode = barcode.trim();
    }
}
