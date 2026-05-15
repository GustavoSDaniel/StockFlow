package com.gustavosdaniel.stock_flow_api.domain.dto.request;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record CategoryRequest(

        @NotBlank(message = "O nome da categoria não pode estar vazio")
        String name,

        @NotBlank(message = "A descrição da categoria não pode estar vazia")
        String description,

        UUID parentId

) {
        public CategoryRequest{

                if (name != null) name = name.trim();

                if (description != null) description = description.trim();

        }

}
