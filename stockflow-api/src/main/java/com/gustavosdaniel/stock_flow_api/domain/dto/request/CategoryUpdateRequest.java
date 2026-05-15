package com.gustavosdaniel.stock_flow_api.domain.dto.request;

public record CategoryUpdateRequest(

        String name,
        String description
) {
    public CategoryUpdateRequest{

        if (name != null) name = name.trim();

        if (description != null) description = description.trim();
    }
}
