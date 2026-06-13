package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository extends R2dbcRepository<Product, UUID> {

    Mono<Boolean> existsBySku(String sku);
}
