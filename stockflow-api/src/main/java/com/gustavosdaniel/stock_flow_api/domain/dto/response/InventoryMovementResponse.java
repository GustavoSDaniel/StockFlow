package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryMovementResponse(

        UUID id,

        LocalDateTime createdAt,

        MovementType movementType,

        MovementReason movementReason,

        Integer quantity,

        Integer quantityBefore,

        Integer quantityAfter,

        String referenceNumber,

        String note,

        UUID supplierId,

        UUID customerId,

        BigDecimal unitCost
) {
}
