package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.NotificationFilter;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.domain.mapping.NotificationMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Notification;
import com.gustavosdaniel.stock_flow_api.repository.NotificationRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    @DisplayName("Find notifications with filter successfully")
    void shouldFilterWithSucesso(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.now();

        Pageable pageable = PageRequest.of(0, 20);

        int limit = pageable.getPageSize();
        Long offset = pageable.getOffset();

        NotificationFilter filter = new NotificationFilter(null, null, null, null, null, null);

        Notification notification = new Notification(
            productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, false, null );

        when(notificationRepository.findAllByFilter(isNull(), isNull(), isNull(),isNull(),
                isNull(), isNull(), eq(limit), eq(offset)))
                .thenReturn(Flux.just(notification));
        when(notificationRepository.countByFilter(isNull(), isNull(), isNull(),isNull(),
                isNull(), isNull())).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findWithFilter(filter, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "O total de notificações na pagina deve ser 1");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByFilter(isNull(), isNull(), isNull(),isNull(),
                isNull(), isNull(), eq(limit), eq(offset));
        verify(notificationRepository).countByFilter(isNull(), isNull(), isNull(),isNull(),
                isNull(), isNull());
        verify(notificationMapper).toNotificationResponse(notification);

    }

    @Test
    @DisplayName("Should find all notifications successfully")
    void shouldAllNotifications(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );

        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllBy(pageable)).thenReturn(Flux.just(notification));
        when(notificationRepository.count()).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findAllNotifications(pageable);

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertEquals(1, result.getTotalElements(), "A quantidade de elementos deve ser o mesmo");
                })
                .verifyComplete();

        verify(notificationRepository).findAllBy(pageable);
        verify(notificationRepository, times(1)).findAllBy(pageable);
        verify(notificationRepository).count();
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find unread notifications successfully")
    void shouldUnreadNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;

        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                false, null, false, null );

        when(notificationRepository.findAllByReadFalse(pageable)).thenReturn(Flux.just(notification));
        when(notificationRepository.countByReadFalse()).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findUnreadNotifications(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "A quantidade de elementos deve ser a mesma");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByReadFalse(pageable);
        verify(notificationRepository, times(1)).findAllByReadFalse(pageable);
        verify(notificationRepository).countByReadFalse();
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find priority notifications successfully")
    void findNotificationWithPriority(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );
        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllByNotificationPriority(priority, pageable))
                .thenReturn(Flux.just(notification));
        when(notificationRepository.countByNotificationPriority(priority)).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findByPriorityNotification(priority, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter apenas 1 elemento");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByNotificationPriority(priority, pageable);
        verify(notificationRepository, times(1))
                .findAllByNotificationPriority(priority, pageable);
        verify(notificationRepository).countByNotificationPriority(priority);
        verify(notificationRepository, times(1)).countByNotificationPriority(priority);
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find type notifications successfully")
    void findTypeNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );
        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllByNotificationType(type, pageable))
                .thenReturn(Flux.just(notification));
        when(notificationRepository.countByNotificationType(type)).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findByTypeNotification(type, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter apenas 1 elemento");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByNotificationType(type, pageable);
        verify(notificationRepository, times(1))
                .findAllByNotificationType(type, pageable);
        verify(notificationRepository).countByNotificationType(type);
        verify(notificationRepository, times(1)).countByNotificationType(type);
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find product notifications successfully")
    void findProductIdNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );
        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllByProductId(productId, pageable))
                .thenReturn(Flux.just(notification));
        when(notificationRepository.countByProductId(productId)).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findByProductNotification(productId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter apenas 1 elemento");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByProductId(productId, pageable);
        verify(notificationRepository, times(1))
                .findAllByProductId(productId, pageable);
        verify(notificationRepository).countByProductId(productId);
        verify(notificationRepository, times(1)).countByProductId(productId);
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find resolved notifications successfully")
    void findResolvedNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );
        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllByResolvedTrue(pageable)).thenReturn(Flux.just(notification));
        when(notificationRepository.countByResolvedTrue()).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findResolvedNotification(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter apenas 1 elemento");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByResolvedTrue(pageable);
        verify(notificationRepository, times(1)).findAllByResolvedTrue(pageable);
        verify(notificationRepository).countByResolvedTrue();
        verify(notificationRepository, times(1)).countByResolvedTrue();
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should find unresolved notifications successfully")
    void findUnresolvedNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.of(2026, 10, 3, 16, 53 );
        LocalDateTime resolvedAt = LocalDateTime.of(2026, 10, 6, 16, 53 );
        Pageable pageable = Pageable.unpaged();

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint,
                true, dateTime, true, resolvedAt );

        when(notificationRepository.findAllByResolvedFalse(pageable)).thenReturn(Flux.just(notification));
        when(notificationRepository.countByResolvedFalse()).thenReturn(Mono.just(1L));
        when(notificationMapper.toNotificationResponse(notification)).thenReturn(response);

        Mono<Page<NotificationResponse>> output = notificationService.findUnresolvedNotifications(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter apenas 1 elemento");
                })
                .verifyComplete();

        verify(notificationRepository).findAllByResolvedFalse(pageable);
        verify(notificationRepository, times(1)).findAllByResolvedFalse(pageable);
        verify(notificationRepository).countByResolvedFalse();
        verify(notificationRepository, times(1)).countByResolvedFalse();
        verify(notificationMapper).toNotificationResponse(notification);
    }

    @Test
    @DisplayName("Should mark notification as read successfully")
    void markedReadNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        when(notificationRepository.findById(id)).thenReturn(Mono.just(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(Mono.just(notification));

        Mono<Void> output = notificationService.markAsRead(id);

        StepVerifier.create(output).verifyComplete();

        verify(notificationRepository).findById(id);
        verify(notificationRepository, times(1)).findById(id);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should mark notification as resolved successfully")
    void markedResolvedNotification(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String title = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minimumQuantity = 10;
        Integer maximumQuantity = 30;
        Integer reorderPoint = 15;

        Notification notification = new Notification(
                productId, productName, sku, type, priority, title, message,
                currentQuantity, minimumQuantity, maximumQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        when(notificationRepository.findById(id)).thenReturn(Mono.just(notification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(Mono.just(notification));

        Mono<Void> output = notificationService.markAsResolved(id);

        StepVerifier.create(output).verifyComplete();

        verify(notificationRepository).findById(id);
        verify(notificationRepository, times(1)).findById(id);
        verify(notificationRepository).save(any(Notification.class));
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }
}