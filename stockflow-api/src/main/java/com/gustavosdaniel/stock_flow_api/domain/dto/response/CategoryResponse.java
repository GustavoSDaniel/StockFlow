package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import java.util.UUID;

public record CategoryResponse (

        UUID id,
        String name,
        String description,
        UUID parentId,
        boolean isActive

) {
}
