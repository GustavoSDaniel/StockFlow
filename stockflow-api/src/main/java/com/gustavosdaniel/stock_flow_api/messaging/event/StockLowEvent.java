package com.gustavosdaniel.stock_flow_api.messaging.event;


import java.time.LocalDateTime;
import java.util.UUID;

public record StockLowEvent(
        UUID eventId,
        LocalDateTime occurredAt,
        UUID productId,
        Integer currentQuantity,
        Integer minimumQuantity
) implements DomainEvent {

    public StockLowEvent(UUID productId, Integer currentQuantity, Integer minimumQuantity){

        this(UUID.randomUUID(), LocalDateTime.now(), productId, currentQuantity, minimumQuantity);
    }

}
