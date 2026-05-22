package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SupplierContactRequest(

        @NotBlank(message = "O nome de contato é obrigatório")
        String contactName,

        @Email(message = "É obrigatório ter o formato de email válido. Exemplo: contato@email.com")
        @NotBlank(message = "O email é obrigatório")
        String email,

        @NotBlank(message = "O telefone do contato é obrigatório")
        @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos")
        String phoneNumber
) {

    public SupplierContactRequest{

        if (contactName != null) contactName = contactName.trim();

    }
}
