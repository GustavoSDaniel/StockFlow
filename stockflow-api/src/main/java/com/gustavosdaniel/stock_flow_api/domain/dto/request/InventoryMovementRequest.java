package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.UUID;


public record InventoryMovementRequest(

        @NotNull(message = "O tipo de movimento é obrigatório")
        MovementType movementType,

        @PositiveOrZero(message = "A quantidade não pode ser negativa")
        @NotNull(message = "A quantidade é obrigatória")
        Integer quantity,

        @NotNull(message = "O motivo do movimento é obrigatório")
        MovementReason movementReason,

        String referenceNumber,

        UUID supplierId,

        UUID customerId,

        String note,

        @PositiveOrZero(message = "O custo unitário não pode ser negativo")
        BigDecimal unitCost

) {
}
