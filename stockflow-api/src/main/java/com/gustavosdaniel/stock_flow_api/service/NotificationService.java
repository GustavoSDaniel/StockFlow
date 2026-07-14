package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.NotificationFilter;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.NotificationResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.domain.mapping.NotificationMapper;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.NotificationNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.NotificationRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class NotificationService {

    private final NotificationMapper notificationMapper;
    private final NotificationRepository notificationRepository;
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    public NotificationService(NotificationMapper notificationMapper, NotificationRepository notificationRepository) {
        this.notificationMapper = notificationMapper;
        this.notificationRepository = notificationRepository;
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findWithFilter(NotificationFilter filter, Pageable pageable){

        String type = filter.type() != null ? filter.type().name() : null;
        String priority = filter.priority() != null ? filter.priority().name() : null;

        return PageUtils.toPage(
            notificationRepository.findAllByFilter(
                    filter.from(),filter.to(),
                    type, priority,
                    filter.read(), filter.resolved(),
                    pageable.getPageSize(), pageable.getOffset()),
                notificationRepository.countByFilter(
                        filter.from(), filter.to(),
                        type, priority,
                        filter.read(), filter.resolved()
                ),
                notificationMapper::toNotificationResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando notificações com filtro: {}", filter))
                .doOnNext(page ->
                        log.info("Total de notificações encontradas: {}", page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findAllNotifications(Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllBy(pageable),
                notificationRepository.count(),
                notificationMapper::toNotificationResponse,
                pageable
        ).doFirst(() -> log.info("Buscando todas as notificações"))
                .doOnNext(page ->
                        log.info("Quantidade de notificações encontradas {}",page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findUnreadNotifications(Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllByReadFalse(pageable),
                notificationRepository.countByReadFalse(),
                notificationMapper::toNotificationResponse,
                pageable
        ).doFirst(() -> log.info("Buscando todas as notificações que ainda não foram lidas"))
                .doOnNext(page ->
                        log.info("Todas as notificações não lidas {} notificações", page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findByPriorityNotification(NotificationPriority priority, Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllByNotificationPriority(priority, pageable),
                notificationRepository.countByNotificationPriority(priority),
                notificationMapper::toNotificationResponse,
                pageable
        ).doFirst(() -> log.info("Buscando notificações por Prioridade"))
                .doOnNext(page ->
                        log.info("Total de {}, notificações encontradas com a Prioridade de: {}",
                                page.getTotalElements(), priority));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findByTypeNotification(NotificationType type, Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllByNotificationType(type, pageable),
                notificationRepository.countByNotificationType(type),
                notificationMapper::toNotificationResponse,
                pageable
        ).doFirst(() -> log.info("Buscando notificações por Tipos"))
                .doOnNext(page ->
                        log.info("Total de {} notificações encontradas com o Tipo de: {}", page.getTotalElements(),
                                type));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findByProductNotification(UUID productId, Pageable pageable){

        return PageUtils.toPage(
                        notificationRepository.findAllByProductId(productId, pageable),
                        notificationRepository.countByProductId(productId),
                        notificationMapper::toNotificationResponse,
                        pageable
                )
                .doFirst(() -> log.info("Buscando notificações do Produto: {}", productId))
                .doOnNext(page ->
                        log.info("Total de: {}, notificações encontradas do Produto: {}",
                                page.getTotalElements(), productId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findResolvedNotification(Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllByResolvedTrue(pageable),
                notificationRepository.countByResolvedTrue(),
                notificationMapper::toNotificationResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando notificações resolvidas"))
                .doOnNext(page -> log.info("Total de notificações resolvidas: {}",
                        page.getTotalElements())
                );
    }

    @Transactional(readOnly = true)
    public Mono<Page<NotificationResponse>> findUnresolvedNotifications(Pageable pageable){

        return PageUtils.toPage(
                notificationRepository.findAllByResolvedFalse(pageable),
                notificationRepository.countByResolvedFalse(),
                notificationMapper::toNotificationResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando todas as notificações não resolvidas"))
                .doOnNext(page ->
                        log.info("Todas as notificações {}, ainda não resolvidas encontradas com sucesso",
                                page.getTotalElements())
                );
    }

    @Transactional
    public Mono<Void> markAsRead(UUID id){

        return notificationRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotificationNotFoundException()))
                .flatMap(notification -> {

                    if (notification.isRead())
                        return Mono.error(new BusinessRuleException("Notificação já marcada como lida"));

                    notification.markAsRead();

                    return notificationRepository.save(notification);
                })
                .doFirst(() -> log.info("Lendo notificação..."))
                .doOnSuccess(notification -> log.info("Notificação: {} lida com sucesso",
                        notification.getTitle()))
                .then();
    }

    @Transactional
    public Mono<Void> markAsResolved(UUID id){

        return notificationRepository.findById(id)
                .switchIfEmpty(Mono.error(new NotificationNotFoundException()))
                .flatMap(notification -> {

                    if (notification.isResolved())
                        return Mono.error(new BusinessRuleException("Notificação já marcada como resolvida"));

                    notification.markAsResolved();

                    return notificationRepository.save(notification);
                })
                .doFirst(() -> log.info("Marcando notificação {} como Resolvida", id))
                .doOnSuccess(notificacao -> log.info("Notificação: {}, resolvida com sucesso",
                        notificacao.getTitle()))
                .then();
    }
}
