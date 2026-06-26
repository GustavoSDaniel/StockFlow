package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;


public record TransferRequest(

        @NotNull(message = "A quantidade é obrigatória")
        @Positive(message = "A quantidade deve ser maior que zero")
        Integer quantity,

        @NotBlank(message = "O armazém de origem é obrigatório")
        @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$",
                message = "Código do armazém inválido")
        String sourceWarehouseId,              // ← adicionado

        @NotBlank(message = "O armazém de destino é obrigatório")
        @Pattern(regexp = "^[A-Z0-9\\-]{2,20}$",
                message = "Código do armazém inválido")
        String targetWarehouseId,

        @NotBlank(message = "O número de referência (ex: Ordem de Transporte) é obrigatório")
        String referenceNumber,

        @NotBlank(message = "As informações adicionais são obrigatórias")
        String note

) { public TransferRequest {

    if (sourceWarehouseId != null) sourceWarehouseId = sourceWarehouseId.toUpperCase().trim();
    if (targetWarehouseId != null) targetWarehouseId = targetWarehouseId.toUpperCase().trim();
    if (referenceNumber != null) referenceNumber = referenceNumber.trim();
    if (note != null) note = note.trim();

    if (sourceWarehouseId != null && sourceWarehouseId.equalsIgnoreCase(targetWarehouseId))
        throw new BusinessRuleException("O armazém de origem e destino não podem ser iguais");

}
}
