package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.StateUF;


import java.util.UUID;

public record AddressResponse(

        UUID id,

        String label,

        String street,

        String streetNumber,

        String complement,

        String neighborhood,

        String city,

        String zipCode,

        StateUF stateUF,

        String country,

        boolean isMain
) {
}
