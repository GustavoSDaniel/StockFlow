package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.NotificationOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.NotificationFilter;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.service.NotificationService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * REST controller for notification management at {@code /api/v1/notifications}.
 * Handles filtering, paging, and status updates (read/resolved) of system notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController implements NotificationOpenApi {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/filter")
    public Mono<ResponseEntity<Page<NotificationResponse>>> notificationFilter(

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime from,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime to,

            @RequestParam(required = false) NotificationType type,
            @RequestParam(required = false) NotificationPriority priority,
            @RequestParam(required = false) Boolean read,
            @RequestParam(required = false) Boolean resolved,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable)
    {
        NotificationFilter filter = new NotificationFilter(from, to, type, priority, read, resolved);

        return notificationService.findWithFilter(filter, pageable).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Page<NotificationResponse>>> allNotifications(

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findAllNotifications(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/unread")
    public Mono<ResponseEntity<Page<NotificationResponse>>> unreadNotifications(

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findUnreadNotifications(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/priority")
    public Mono<ResponseEntity<Page<NotificationResponse>>> priorityNotifications(

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,

            @RequestParam NotificationPriority priority
    ){
        return notificationService.findByPriorityNotification(priority, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/type")
    public Mono<ResponseEntity<Page<NotificationResponse>>> typeNotifications(

            @RequestParam NotificationType type,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findByTypeNotification(type, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/{productId}/product")
    public Mono<ResponseEntity<Page<NotificationResponse>>> productNotification(
            @PathVariable UUID productId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findByProductNotification(productId, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/resolved")
    public Mono<ResponseEntity<Page<NotificationResponse>>> resolvedNotification(

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findResolvedNotification(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/unresolved")
    public Mono<ResponseEntity<Page<NotificationResponse>>> unresolvedNotification(

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ){
        return notificationService.findUnresolvedNotifications(pageable).map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/read")
    public Mono<ResponseEntity<Void>> readNotification(@PathVariable UUID id){

        return notificationService.markAsRead(id).thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/resolved")
    public Mono<ResponseEntity<Void>> resolvedNotification(@PathVariable UUID id){

        return notificationService.markAsResolved(id).thenReturn(ResponseEntity.noContent().build());
    }
}
