package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface StockRepository extends R2dbcRepository<Stock, UUID> {

    Mono<Boolean> existsByProductId(UUID productId);

    Mono<Stock> findStockByProductId(UUID productId);

    Flux<Stock> findAllBy(Pageable pageable);

    @Query("SELECT * FROM stocks WHERE current_quantity = 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findOutOfStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity = 0")
    Mono<Long> countOutOfStock();

    @Query("SELECT * FROM stocks WHERE current_quantity <= minimum_quantity " +
            "AND current_quantity > 0 LIMIT :limit OFFSET :offset")
    Flux<Stock> findLowStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity <= minimum_quantity AND current_quantity > 0")
    Mono<Long> countLowStock();

    @Query("SELECT * FROM stocks WHERE current_quantity > maximum_quantity LIMIT :limit OFFSET :offset")
    Flux<Stock> findOverStock(int limit, Long offset);

    @Query("SELECT COUNT(id) FROM stocks WHERE current_quantity > maximum_quantity")
    Mono<Long> countOverStock();
}
