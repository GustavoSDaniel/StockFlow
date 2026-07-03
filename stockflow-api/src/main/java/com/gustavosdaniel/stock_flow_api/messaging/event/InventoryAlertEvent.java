package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryAlertEvent(

        UUID eventId,
        LocalDateTime occurredAt,
        UUID productId,
        NotificationType notificationType,
        Integer currentQuantity,
        Integer minimumQuantity,
        Integer maximumQuantity,
        Integer reorderPoint
) implements DomainEvent{

    public InventoryAlertEvent(UUID productId, NotificationType notificationType, Integer currentQuantity, Integer minimumQuantity, Integer maximumQuantity, Integer reorderPoint){

        this(UUID.randomUUID(), LocalDateTime.now(), productId, notificationType, currentQuantity, minimumQuantity, maximumQuantity, reorderPoint);
    }

}
