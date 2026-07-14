package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import java.util.UUID;

public record SupplierSummaryResponse (

        UUID id,
        String cnpj,
        String name,
        String tradeName
){
}
