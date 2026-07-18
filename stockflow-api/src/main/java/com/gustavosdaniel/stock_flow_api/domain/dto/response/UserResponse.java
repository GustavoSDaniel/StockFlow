package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(

        UUID id,
        String userName,
        UserRole role,
        boolean active,
        LocalDateTime createdAt
) {
}
