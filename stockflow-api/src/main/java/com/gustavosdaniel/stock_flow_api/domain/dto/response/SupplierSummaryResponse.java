package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import java.util.UUID;

public record SupplierSummaryResponse (

        UUID id,
        String Cnpj,
        String name,
        String tradeName
){
}
