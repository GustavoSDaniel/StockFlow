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
    @DisplayName("FInd filter notification with sucesso")
    void shouldFilterWithSucesso(){

        UUID id = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        NotificationType type = NotificationType.STOCK_LOW;
        NotificationPriority priority = NotificationPriority.HIGH;
        String titulo = "Estoque baixo";
        String message = "Produto com estoque baixo detectado";
        Integer currentQuantity = 7;
        Integer minumuQuantity = 10;
        Integer maximuQuantity = 30;
        Integer reorderPoint = 15;
        LocalDateTime dateTime = LocalDateTime.now();

        Pageable pageable = PageRequest.of(0, 20);

        int limit = pageable.getPageSize();
        Long offset = pageable.getOffset();

        NotificationFilter filter = new NotificationFilter(null, null, null, null, null, null);

        Notification notification = new Notification(
            productId, productName, sku, type, priority, titulo, message,
                currentQuantity, minumuQuantity, maximuQuantity, reorderPoint
        );
        ReflectionTestUtils.setField(notification, "id", id);

        NotificationResponse response = new NotificationResponse(
                id, productId, productName, sku, type, priority, titulo, message,
                currentQuantity, minumuQuantity, maximuQuantity, reorderPoint,
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

}