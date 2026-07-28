package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.OutboxEvent;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.UUID;

/**
 * Reactive repository for the {@code outbox_events} table.
 * <p>
 * Provides queries for the {@code OutboxScheduler} to fetch pending events
 * and update their processing status.
 * </p>
 */
@Repository
public interface OutboxEventRepository extends ReactiveCrudRepository<OutboxEvent, UUID> {

    /**
     * Finds up to {@code limit} unprocessed events ordered by creation date
     * ascending (FIFO).
     *
     * @param limit maximum number of events to return
     * @return a {@link Flux} of pending {@link OutboxEvent} entities
     */
    @Query("SELECT * FROM outbox_events WHERE processed = FALSE ORDER BY created_at ASC LIMIT :limit")
    Flux<OutboxEvent> findPendingEvents(int limit);
}
