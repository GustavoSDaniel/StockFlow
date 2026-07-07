package com.gustavosdaniel.stock_flow_api.controller.OpenApi;


import java.time.LocalDateTime;
import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.NotificationFilter;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

@Tag(name = "Notifications", description = "Alertas e notificações")
public interface NotificationOpenApi {

    @Operation(
            summary = "Filtrar notificações",
            description = "Retorna uma página de notificações aplicando múltiplos filtros opcionais (período, tipo, prioridade, lida, resolvida)"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notificações filtradas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> notificationFilter(
            @Parameter(description = "Data inicial (ISO 8601)", example = "2025-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,

            @Parameter(description = "Data final (ISO 8601)", example = "2025-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,

            @Parameter(description = "Tipo da notificação", example = "LOW_STOCK")
            @RequestParam(required = false) NotificationType type,

            @Parameter(description = "Prioridade da notificação", example = "HIGH")
            @RequestParam(required = false) NotificationPriority priority,

            @Parameter(description = "Status de leitura (true = lida, false = não lida)")
            @RequestParam(required = false) Boolean read,

            @Parameter(description = "Status de resolução (true = resolvida, false = não resolvida)")
            @RequestParam(required = false) Boolean resolved,

            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Listar todas as notificações",
            description = "Retorna uma página com todas as notificações, ordenadas pela data de criação (mais recentes primeiro)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> allNotifications(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Notificações não lidas",
            description = "Retorna apenas notificações que ainda não foram marcadas como lidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações não lidas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> unreadNotifications(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Notificações por prioridade",
            description = "Filtra notificações pela prioridade informada")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações pela prioridade"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> priorityNotifications(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable,
            @Parameter(description = "Prioridade", required = true, example = "HIGH")
            @RequestParam NotificationPriority priority);

    @Operation(summary = "Notificações por tipo",
            description = "Filtra notificações pelo tipo informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações pelo tipo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> typeNotifications(
            @Parameter(description = "Tipo da notificação", required = true, example = "LOW_STOCK")
            @RequestParam NotificationType type,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Notificações por produto",
            description = "Retorna notificações associadas a um produto específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações do produto"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> productNotification(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Notificações resolvidas",
            description = "Retorna notificações que já foram marcadas como resolvidas")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações resolvidas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> resolvedNotification(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Notificações não resolvidas",
            description = "Retorna notificações pendentes de resolução")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de notificações não resolvidas"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<NotificationResponse>>> unresolvedNotification(
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable);

    @Operation(summary = "Marcar notificação como lida",
            description = "Altera o status de leitura da notificação para 'lida'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificação marcada como lida"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Void>> readNotification(
            @Parameter(description = "ID da notificação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    @Operation(summary = "Marcar notificação como resolvida",
            description = "Altera o status de resolução da notificação para 'resolvida'")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Notificação resolvida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Notificação não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Void>> resolvedNotification(
            @Parameter(description = "ID da notificação", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);
}
