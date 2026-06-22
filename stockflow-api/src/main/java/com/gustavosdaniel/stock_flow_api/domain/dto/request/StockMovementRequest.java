package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockMovementRequest(

        @NotNull(message = "A quantidade é obrigatória")
        @PositiveOrZero(message = "Não é possivel enviar uma quantidade negativa")
        Integer quantity
) {
}
