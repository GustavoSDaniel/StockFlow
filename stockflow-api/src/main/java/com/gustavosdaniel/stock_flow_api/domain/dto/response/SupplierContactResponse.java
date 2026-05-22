package com.gustavosdaniel.stock_flow_api.domain.dto.response;



import java.util.UUID;

public record SupplierContactResponse(

        UUID id,

        String contactName,

        String email,

        String phoneNumber
) {
}
