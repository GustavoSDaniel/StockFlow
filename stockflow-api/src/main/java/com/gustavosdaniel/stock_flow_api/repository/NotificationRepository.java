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

public interface NotificationRepository extends R2dbcRepository<Notification, UUID> {

    Flux<Notification> findAllBy(Pageable pageable);

    Flux<Notification> findAllByReadFalse(Pageable pageable);

    Mono<Long> countByReadFalse();

    Flux<Notification> findAllByNotificationPriority(NotificationPriority priority, Pageable pageable);

    Mono<Long> countByNotificationPriority(NotificationPriority priority);

    Flux<Notification> findAllByNotificationType(NotificationType type, Pageable pageable);

    Mono<Long> countByNotificationType(NotificationType type);

    Flux<Notification> findAllByProductId(UUID productId, Pageable pageable);

    Mono<Long> countByProductId(UUID productId);

    Mono<Boolean> existsByProductId(UUID productId);

    Flux<Notification> findAllByResolvedTrue(Pageable pageable);

    Mono<Long> countByResolvedTrue();

    Flux<Notification> findAllByResolvedFalse(Pageable pageable);

    Mono<Long> countByResolvedFalse();

    Mono<Long> countByNotificationPriorityInAndResolvedFalse(Collection<NotificationPriority> priority);

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
