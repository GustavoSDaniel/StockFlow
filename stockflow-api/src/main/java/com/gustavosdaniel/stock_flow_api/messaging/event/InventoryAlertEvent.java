package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonCreator
    public InventoryAlertEvent(
            @JsonProperty("eventId") UUID eventId,
            @JsonProperty("occurredAt") LocalDateTime occurredAt,
            @JsonProperty("productId") UUID productId,
            @JsonProperty("notificationType") NotificationType notificationType,
            @JsonProperty("currentQuantity") Integer currentQuantity,
            @JsonProperty("minimumQuantity") Integer minimumQuantity,
            @JsonProperty("maximumQuantity") Integer maximumQuantity,
            @JsonProperty("reorderPoint") Integer reorderPoint
    ){
        this.eventId = eventId;
        this.occurredAt = occurredAt;
        this.productId = productId;
        this.notificationType = notificationType;
        this.currentQuantity = currentQuantity;
        this.minimumQuantity = minimumQuantity;
        this.maximumQuantity = maximumQuantity;
        this.reorderPoint = reorderPoint;
    }

    public InventoryAlertEvent(UUID productId, NotificationType notificationType, Integer currentQuantity, Integer minimumQuantity, Integer maximumQuantity, Integer reorderPoint){

        this(UUID.randomUUID(), LocalDateTime.now(), productId, notificationType, currentQuantity, minimumQuantity, maximumQuantity, reorderPoint);
    }

}
