package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.AddressResponse;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.List;

public record SupplierUpdateRequest(

        String tradeName,

        String website,

        @PositiveOrZero(message = "O valor não pode ser negativo")
        BigDecimal minOrderValue,

        String notes

) {
    public SupplierUpdateRequest{

        if (tradeName != null) tradeName = tradeName.trim();
        if (website != null) website = website.trim();
        if (notes != null) notes = notes.trim();
    }
}
