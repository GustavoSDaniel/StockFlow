package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdate(

        @PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
        Integer minimumQuantity,

        @PositiveOrZero(message = "A quantidade máxima não pode ser negativa")
        Integer maximumQuantity,

        @PositiveOrZero(message = "O ponto de reposição não pode ser negativo")
        Integer reorderPoint,

        @PositiveOrZero(message = "A quantidade de reposição não pode ser negativa")
        Integer reorderQuantity,

        String location,

        @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$",
                message = "Código do armazém deve conter entre 2 e 20 caracteres")
        String warehouseId
) {
    public StockUpdate {

        if (location != null) location = location.trim().toUpperCase();
        if (warehouseId != null) warehouseId = warehouseId.trim().toUpperCase();
    }
}
