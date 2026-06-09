package com.gustavosdaniel.stock_flow_api.domain.dto.request;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record SupplierRequest(

        @NotBlank(message = "O nome do fornecedor é obrigatório")
        String name,

        @NotBlank(message = "O CNPJ é obrigatório")
        @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos numéricos")
        String cnpj,

        @NotBlank(message = "O nome fantasia é obrigatório")
        String tradeName,

        @Valid
        @NotEmpty(message = "As informações de contato são obrigatórias")
        List<SupplierContactRequest> contacts,

        String website,

        @NotNull(message = "É obrigatório informar um valor minimo para esse fornecedor")
        @PositiveOrZero(message = "O valor não pode ser negativo")
        BigDecimal minOrderValue,

        String notes,

        @Valid
        @NotEmpty(message = "É obrigatório informar um endereço para esse fornecedor")
        List<AddressRequest> addresses
) {
    public SupplierRequest{

        if (name != null) name = name.trim();
        if (tradeName != null) tradeName = tradeName.trim();
        if (cnpj != null) cnpj = cnpj.trim().replaceAll("[^0-9]", "");
        if (website != null) website = website.trim();
    }
}
