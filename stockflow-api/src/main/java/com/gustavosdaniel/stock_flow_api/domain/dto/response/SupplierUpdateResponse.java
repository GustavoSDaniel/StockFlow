package com.gustavosdaniel.stock_flow_api.domain.dto.response;


import java.math.BigDecimal;
import java.util.UUID;

public record SupplierUpdateResponse(

        UUID id,

        String name,

        String cnpj,

        String tradeName,

        String website,

        BigDecimal minOrderValue,

        String notes

) {
}
