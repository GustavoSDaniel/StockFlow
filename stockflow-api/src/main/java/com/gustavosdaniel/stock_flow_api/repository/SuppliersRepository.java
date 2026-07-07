package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface SuppliersRepository extends R2dbcRepository<Supplier, UUID> {

    Flux<Supplier> findAllBy(Pageable pageable);

    Mono<Boolean> existsByCnpj(String cnpj);

    Mono<Supplier> findByCnpj(String cnpj);

    @Query("SELECT * FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Flux<Supplier> searchByName(String name, Pageable pageable);

    @Query("SELECT COUNT(*) FROM suppliers WHERE name ILIKE CONCAT('%', :name, '%')")
    Mono<Long> countByName(String name);

    @Query("SELECT * FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Flux<Supplier> searchByTradeName(String tradeName, Pageable pageable);

    @Query("SELECT COUNT(*) FROM suppliers WHERE trade_name ILIKE CONCAT('%', :tradeName, '%')")
    Mono<Long> countByTradeName(String tradeName);

}
