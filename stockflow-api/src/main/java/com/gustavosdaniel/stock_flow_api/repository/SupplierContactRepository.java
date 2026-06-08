package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SupplierContactRepository extends R2dbcRepository<SupplierContact, UUID> {

    Flux<SupplierContact> findAllBySupplierId(UUID supplierId);
    Mono<Void> deleteAllBySupplierId(UUID supplierId);
}
