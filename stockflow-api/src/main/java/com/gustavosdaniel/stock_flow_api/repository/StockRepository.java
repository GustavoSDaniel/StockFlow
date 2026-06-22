package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StockRepository extends R2dbcRepository<Stock, UUID> {

    Mono<Boolean> existsByProductId(UUID productId);

    Mono<Stock> findStockByProductId(UUID productId);

    Flux<Stock> findAllBy(Pageable pageable);

}
