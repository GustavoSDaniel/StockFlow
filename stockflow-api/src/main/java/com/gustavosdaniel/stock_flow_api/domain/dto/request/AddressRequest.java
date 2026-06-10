package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;


public record AddressRequest(

        String label,

        String street,

        @NotBlank(message = "É obrigatório informar o número do estabelecimento")
        String streetNumber,

        String complement,

        String neighborhood,

        String city,

        @NotBlank(message = "O CEP do fornecedor é obrigatório")
        @Pattern(regexp = "\\d{8}", message = "CEP deve conter 8 dígitos numéricos")
        String zipCode,

        StateUF stateUF,

        @NotBlank(message = "É obrigatório informar o país")
        String country,

        @NotNull(message = "É necessário informar se este é o endereço principal")
        Boolean isMain
) {
        public AddressRequest {

                if (street != null) street = street.trim();
                if (streetNumber != null) streetNumber = streetNumber.trim();
                if (neighborhood != null) neighborhood = neighborhood.trim();
                if (city != null) city = city.trim();
                if (zipCode != null) zipCode = zipCode.trim().replaceAll("[^0-9]", "");
                if (country != null) country = country.trim();
                if (label != null) label = label.trim();
        }

        public boolean hasManualAddress(){

                return street != null && !street.isBlank() &&
                        neighborhood != null && !neighborhood.isBlank() &&
                        city != null && !city.isBlank() &&
                        stateUF != null;
        }
}
