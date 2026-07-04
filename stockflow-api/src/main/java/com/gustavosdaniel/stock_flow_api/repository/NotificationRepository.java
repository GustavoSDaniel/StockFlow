package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.domain.po.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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
}
