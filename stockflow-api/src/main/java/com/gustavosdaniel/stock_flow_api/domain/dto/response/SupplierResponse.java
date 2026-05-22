package com.gustavosdaniel.stock_flow_api.domain.dto.response;


import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SupplierResponse(

        UUID id,

        String name,

        String cnpj,

        String tradeName,

        List<SupplierContactResponse> contacts,

        String website,

        BigDecimal minOrderValue,

        String notes,

        List<AddressResponse> addresses
) {
}
