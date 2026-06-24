package com.gustavosdaniel.stock_flow_api.messaging.event;

import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de domínio gerados por {@link InventoryMovement}.
 * <p>
 * Atualmente, os eventos são apenas logados. Quando um message broker
 * (RabbitMQ, Kafka, etc.) for integrado, este componente será o ponto
 * único de substituição — basta trocar a implementação interna.
 * </p>
 */
@Component
public class StockEventPublisher {

    private final Logger log = LoggerFactory.getLogger(StockEventPublisher.class);

    /**
     * Publica todos os eventos de domínio pendentes da movimentação.
     * <p>
     * Chamado imediatamente após a persistência da movimentação.
     * Os eventos devem ser limpos ({@link InventoryMovement#clearDomainEvent()})
     * pelo chamador após a publicação.
     * </p>
     *
     * @param movement movimentação que gerou os eventos
     */
    public void publish(InventoryMovement movement) {
        if (movement == null) return;

        movement.getDomainEvents().forEach(event -> {
            if (event instanceof StockLowEvent lowEvent) {
                log.warn("StockLowEvent — productId={}, currentQty={}, minQty={}",
                        lowEvent.productId(), lowEvent.currentQuantity(),
                        lowEvent.minimumQuantity());
                // Futuro: rabbitTemplate.convertAndSend("stock.low", lowEvent);
            } else {
                log.info("DomainEvent — type={}, eventId={}",
                        event.getClass().getSimpleName(), event.eventId());
            }
        });
    }
}
