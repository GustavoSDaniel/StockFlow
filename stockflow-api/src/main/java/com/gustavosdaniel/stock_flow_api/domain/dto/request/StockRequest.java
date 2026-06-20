package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record StockRequest(

        @PositiveOrZero(message = "A quantidade mínima não pode ser negativa")
        @NotNull(message = "Informar a quantidade mínima é obrigatório")
        Integer minimumQuantity,

        @PositiveOrZero(message = "A quantidade máxima não pode ser negativa")
        @NotNull(message = "Informar a quantidade máxima é obrigatório")
        Integer maximumQuantity,

        @PositiveOrZero(message = "O ponto de reposição não pode ser negativo")
        @NotNull(message = "Informar o ponto de reposição é obrigatório")
        Integer reorderPoint,

        @Positive(message = "A quantidade de reposição deve ser maior que zero")
        @NotNull(message = "Informar a quantidade de reposição é obrigatório")
        Integer reorderQuantity,

        @NotBlank(message = "A localização é obrigatória")
        String location,

        @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$",
                message = "Código do armazém deve conter entre 2 e 20 caracteres")
        @NotBlank(message = "O armazém é obrigatório")
        String warehouseId
) {
    public StockRequest {

        if (location != null) location = location.trim().toLowerCase();
        if (warehouseId != null) warehouseId = warehouseId.trim().toLowerCase();
    }
}
