package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain event emitted when a product's inventory requires attention.
 * <p>
 * Covers four alert scenarios: out of stock, low stock, reorder point reached,
 * and overstock. Published via Kafka to the {@code stockflow.inventory.alerts}
 * topic and consumed by {@link NotificationConsumer}.
 * </p>
 *
 * @param eventId          unique identifier of the event
 * @param occurredAt       timestamp when the event was generated
 * @param productId        the affected product's unique identifier
 * @param notificationType the type of inventory alert
 * @param currentQuantity  current stock quantity at the time of the event
 * @param minimumQuantity  the product's configured minimum quantity
 * @param maximumQuantity  the product's configured maximum quantity
 * @param reorderPoint     the product's configured reorder point
 */
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

    /**
     * JSON deserialization constructor. All parameters are annotated with
     * {@link JsonProperty} for Jackson mapping.
     */
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
