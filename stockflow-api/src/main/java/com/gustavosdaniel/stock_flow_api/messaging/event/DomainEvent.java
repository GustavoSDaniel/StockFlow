package com.gustavosdaniel.stock_flow_api.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

public interface DomainEvent {

    UUID eventId();
    LocalDateTime occurredAt();
}
