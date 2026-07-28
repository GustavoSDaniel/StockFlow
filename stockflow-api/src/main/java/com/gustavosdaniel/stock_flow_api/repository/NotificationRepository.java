package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.domain.po.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.UUID;

/**
 * Reactive repository for {@link Notification} entities.
 * <p>
 * Provides queries for listing, filtering, and counting notifications by
 * read status, resolved status, priority, type, product, and a flexible
 * multi-criteria filter.
 * </p>
 */
public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {

    /**
     * Finds all notifications with pagination.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of {@link Notification} entities
     */
    Flux<Notification> findAllBy(Pageable pageable);

    /**
     * Finds all unread notifications.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of unread {@link Notification} entities
     */
    Flux<Notification> findAllByReadFalse(Pageable pageable);

    /**
     * Counts unread notifications.
     *
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByReadFalse();

    /**
     * Finds all notifications with the given priority.
     *
     * @param priority the {@link NotificationPriority} to filter by
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Notification} entities
     */
    Flux<Notification> findAllByNotificationPriority(NotificationPriority priority, Pageable pageable);

    /**
     * Counts notifications with the given priority.
     *
     * @param priority the {@link NotificationPriority} to filter by
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByNotificationPriority(NotificationPriority priority);

    /**
     * Finds all notifications of the given type.
     *
     * @param type     the {@link NotificationType} to filter by
     * @param pageable pagination parameters
     * @return a {@link Flux} of matching {@link Notification} entities
     */
    Flux<Notification> findAllByNotificationType(NotificationType type, Pageable pageable);

    /**
     * Counts notifications of the given type.
     *
     * @param type the {@link NotificationType} to filter by
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByNotificationType(NotificationType type);

    /**
     * Finds all notifications for the given product.
     *
     * @param productId the product's unique identifier
     * @param pageable  pagination parameters
     * @return a {@link Flux} of matching {@link Notification} entities
     */
    Flux<Notification> findAllByProductId(UUID productId, Pageable pageable);

    /**
     * Counts notifications for the given product.
     *
     * @param productId the product's unique identifier
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByProductId(UUID productId);

    /**
     * Checks whether any notification exists for the given product.
     *
     * @param productId the product's unique identifier
     * @return a {@link Mono} emitting {@code true} if at least one exists
     */
    Mono<Boolean> existsByProductId(UUID productId);

    /**
     * Finds all resolved notifications.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of resolved {@link Notification} entities
     */
    Flux<Notification> findAllByResolvedTrue(Pageable pageable);

    /**
     * Counts resolved notifications.
     *
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByResolvedTrue();

    /**
     * Finds all unresolved notifications.
     *
     * @param pageable pagination parameters
     * @return a {@link Flux} of unresolved {@link Notification} entities
     */
    Flux<Notification> findAllByResolvedFalse(Pageable pageable);

    /**
     * Counts unresolved notifications.
     *
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByResolvedFalse();

    /**
     * Counts unresolved notifications whose priority is in the given set.
     *
     * @param priority a collection of {@link NotificationPriority} values
     * @return a {@link Mono} emitting the count
     */
    Mono<Long> countByNotificationPriorityInAndResolvedFalse(Collection<NotificationPriority> priority);

    /**
     * Finds notifications matching the given multi-criteria filter.
     * <p>
     * Each nullable parameter acts as an optional filter. Results are ordered
     * by creation date (newest first) and paginated with {@code limit} and {@code offset}.
     * </p>
     *
     * @param from     optional start of the creation date range (inclusive)
     * @param to       optional end of the creation date range (inclusive)
     * @param type     optional notification type filter
     * @param priority optional notification priority filter
     * @param read     optional read status filter
     * @param resolved optional resolved status filter
     * @param limit    maximum number of records to return
     * @param offset   number of records to skip
     * @return a {@link Flux} of matching {@link Notification} entities
     */
    @Query("""
        SELECT * FROM notifications
        WHERE
            (:from::timestamp IS NULL OR created_at >= :from)
        AND (:to::timestamp   IS NULL OR created_at <= :to)
        AND (:type::varchar IS NULL OR notification_type = CAST(:type AS notification_type))
        AND (:priority::varchar IS NULL OR notification_priority = CAST(:priority AS notification_priority))
        AND (:read::boolean IS NULL OR is_read = :read)
        AND (:resolved::boolean IS NULL OR is_resolved = :resolved)
        ORDER BY created_at DESC
        LIMIT :limit OFFSET :offset
        """)
    Flux<Notification> findAllByFilter(
            LocalDateTime from,
            LocalDateTime to,
            String type,
            String priority,
            Boolean read,
            Boolean resolved,
            int limit,
            Long offset
    );

    /**
     * Counts notifications matching the given multi-criteria filter.
     * <p>
     * Each nullable parameter acts as an optional filter.
     * </p>
     *
     * @param from     optional start of the creation date range (inclusive)
     * @param to       optional end of the creation date range (inclusive)
     * @param type     optional notification type filter
     * @param priority optional notification priority filter
     * @param read     optional read status filter
     * @param resolved optional resolved status filter
     * @return a {@link Mono} emitting the count
     */
    @Query("""
    SELECT COUNT(*) FROM notifications
    WHERE
        (:from::timestamp IS NULL OR created_at >= :from)
    AND (:to::timestamp   IS NULL OR created_at <= :to)
    AND (:type::varchar IS NULL OR notification_type = CAST(:type AS notification_type))
    AND (:priority::varchar IS NULL OR notification_priority = CAST(:priority AS notification_priority))
    AND (:read::boolean IS NULL OR is_read = :read)
    AND (:resolved::boolean IS NULL OR is_resolved = :resolved)
    """)
    Mono<Long> countByFilter(
            LocalDateTime from,
            LocalDateTime to,
            String type,
            String priority,
            Boolean read,
            Boolean resolved
    );
}
