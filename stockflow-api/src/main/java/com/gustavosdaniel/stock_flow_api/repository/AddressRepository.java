package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link Address} entities.
 * <p>
 * Provides queries to find and delete addresses associated with a supplier.
 * </p>
 */
public interface AddressRepository extends R2dbcRepository<Address, UUID> {

    /**
     * Finds all addresses belonging to the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @return a {@link Flux} of {@link Address} entities
     */
    Flux<Address> findAllBySupplierId(UUID supplierId);

    /**
     * Deletes all addresses associated with the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @return a {@link Mono} that completes when deletion is done
     */
    Mono<Void> deleteAllBySupplierId(UUID supplierId);
}
