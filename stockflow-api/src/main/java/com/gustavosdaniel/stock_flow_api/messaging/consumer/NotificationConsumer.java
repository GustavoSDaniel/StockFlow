package com.gustavosdaniel.stock_flow_api.messaging.consumer;

import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationType;
import com.gustavosdaniel.stock_flow_api.domain.po.Notification;
import com.gustavosdaniel.stock_flow_api.messaging.event.*;
import com.gustavosdaniel.stock_flow_api.repository.NotificationRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;

@Component
public class NotificationConsumer {

    private final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);
    private final KafkaReceiver<String, Object> receiver;
    private final NotificationRepository notificationRepository;
    private final ProductRepository productRepository;


    public NotificationConsumer(KafkaReceiver<String, Object> receiver, NotificationRepository notificationRepository, ProductRepository productRepository) {
        this.receiver = receiver;
        this.notificationRepository = notificationRepository;
        this.productRepository = productRepository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startListening(){

        log.info("Iniciando o consumidor do Kafka para alertas de estoque...");

        receiver.receive()
                .flatMap(this::processRecord)
                .subscribe();
    }

    private Mono<Void> processRecord(ReceiverRecord<String, Object> record){

        Object payload = record.value();

        if (payload instanceof InventoryAlertEvent alertEvent){

            return productRepository.findById(alertEvent.productId())
                    .flatMap(product -> {
                        Notification notification = buildNotification(
                                alertEvent, product.getName(), product.getSku());

                        return notificationRepository.save(notification);
                    })
                    .doOnSuccess(saved -> log.info("Notificação salva com sucesso para o produto: {}", alertEvent.productId()))
                    .doOnError(e -> log.error("Erro ao salvar notificação: {}", e.getMessage()))
                    .then(Mono.fromRunnable(() -> record.receiverOffset().acknowledge()));
        }

        record.receiverOffset().acknowledge();
        return Mono.empty();
    }

    private Notification buildNotification(
            InventoryAlertEvent alertEvent, String productName, String sku
    ){
        return switch (alertEvent.notificationType()){

            case OUT_OF_STOCK -> new Notification(
                    alertEvent.productId(), productName, sku,
                    NotificationType.OUT_OF_STOCK, NotificationPriority.CRITICAL,
                    "Estoque Zerado!",
                    String.format("O produto %s esgotou completamente.", productName),
                    alertEvent.currentQuantity(),
                    alertEvent.minimumQuantity(),
                    alertEvent.maximumQuantity(),
                    alertEvent.reorderPoint(),
                    null
            );

            case STOCK_LOW -> new Notification(
                    alertEvent.productId(), productName, sku,
                    NotificationType.STOCK_LOW, NotificationPriority.HIGH,
                    "Estoque Baixo !",
                    String.format("O produto %s atingiu a quantidade mínima (%d unidades).", productName, alertEvent.minimumQuantity()),
                    alertEvent.currentQuantity(),
                    alertEvent.minimumQuantity(),
                    alertEvent.maximumQuantity(),
                    alertEvent.reorderPoint(),
                    null
            );

            case REORDER_POINT -> new Notification(

                    alertEvent.productId(), productName, sku,
                    NotificationType.REORDER_POINT, NotificationPriority.LOW,
                    "Ponto de Reposição",
                    String.format("O produto %s atingiu a quantidade de reposição (%d unidades).", productName, alertEvent.reorderPoint()),
                    alertEvent.currentQuantity(),
                    alertEvent.minimumQuantity(),
                    alertEvent.maximumQuantity(),
                    alertEvent.reorderPoint(),
                    null
            );


            case OVERSTOCK -> new Notification(
                    alertEvent.productId(), productName, sku,
                    NotificationType.OVERSTOCK, NotificationPriority.MEDIUM,
                    "Estoque Excedente",
                    String.format("O produto %s ultrapassou o limite máximo estipulado.", productName),
                    alertEvent.currentQuantity(),
                    alertEvent.minimumQuantity(),
                    alertEvent.maximumQuantity(),
                    alertEvent.reorderPoint(),
                    null
            );

            default -> throw new IllegalArgumentException("Tipo de alerta desconhecido: " + alertEvent.getClass().getSimpleName());
        };
    }
}
