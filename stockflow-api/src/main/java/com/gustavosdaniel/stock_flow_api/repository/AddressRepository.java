package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface AddressRepository extends R2dbcRepository<Address, UUID> {

    Flux<Address> findAllBySupplierId(UUID supplierId);
    Mono<Void> deleteAllBySupplierId(UUID supplierId);
}
