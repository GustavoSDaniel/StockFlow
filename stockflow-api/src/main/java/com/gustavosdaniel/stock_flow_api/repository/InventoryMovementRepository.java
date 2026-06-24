package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface InventoryMovementRepository extends R2dbcRepository<InventoryMovement, UUID> {

    Flux<InventoryMovement> findAllByStockId(UUID stockId, Pageable pageable);

    Mono<Long> countByStockId(UUID stockId);
}
