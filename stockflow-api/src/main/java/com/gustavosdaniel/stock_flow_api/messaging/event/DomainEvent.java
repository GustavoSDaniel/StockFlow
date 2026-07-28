package com.gustavosdaniel.stock_flow_api.messaging.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Marker interface for domain events in the system.
 * <p>
 * Every domain event must carry a unique identifier and the timestamp
 * at which it occurred, enabling traceability across the messaging pipeline.
 * </p>
 */
public interface DomainEvent {

    /**
     * Returns the unique identifier of this event.
     *
     * @return the event UUID
     */
    UUID eventId();

    /**
     * Returns the timestamp when this event was created.
     *
     * @return the occurrence timestamp
     */
    LocalDateTime occurredAt();
}
