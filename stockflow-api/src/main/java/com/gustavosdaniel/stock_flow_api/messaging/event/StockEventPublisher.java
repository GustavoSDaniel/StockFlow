package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import com.gustavosdaniel.stock_flow_api.messaging.producer.StockEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Publicador de eventos de domínio gerados por {@link InventoryMovement}.
 * <p>
 * Publica eventos de domínio via Kafka utilizando {@link StockEventProducer}.
 * </p>
 */
@Component
public class StockEventPublisher {

    private final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);
    private final StockEventProducer stockEventProducer;

    public StockEventPublisher(StockEventProducer stockEventProducer) {
        this.stockEventProducer = stockEventProducer;
    }

    public Mono<Void> publish(InventoryMovement movement) {

        if (movement == null || movement.getDomainEvents().isEmpty()) return Mono.empty();

        return Flux.fromIterable(movement.getDomainEvents())
                .flatMap(event -> {
                    if (event instanceof  InventoryAlertEvent alertEvent) {

                        log.warn("Preparando envio para o Kafka: {}  productId={}",
                                alertEvent.getClass().getSimpleName(), alertEvent.productId());

                        return stockEventProducer.sendInventoryAlert(alertEvent);
                    } else {

                        log.info("DomainEvent ignorado (sem integração Kafka) type={}, eventId={}",
                                event.getClass().getSimpleName(), event.eventId());
                        return Mono.empty();
                    }
                })
                .then();
    }
}
