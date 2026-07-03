package com.gustavosdaniel.stock_flow_api.domain.dto.response;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(

        UUID id,
        UUID productId,
        String productName,
        String productSku,
        NotificationType notificationType,
        NotificationPriority notificationPriority,
        String title,
        String message,
        Integer currentQuantity,
        Integer minimumQuantity,
        Integer maximumQuantity,
        Integer reorderPoint,
        UUID assignedTo,
        boolean read,
        LocalDateTime readAt,
        boolean resolved,
        LocalDateTime resolvedAt

) {
}
