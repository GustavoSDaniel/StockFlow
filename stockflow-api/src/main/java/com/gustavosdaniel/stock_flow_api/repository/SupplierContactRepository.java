package com.gustavosdaniel.stock_flow_api.repository;

import com.gustavosdaniel.stock_flow_api.domain.po.Address;
import com.gustavosdaniel.stock_flow_api.domain.po.SupplierContact;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Reactive repository for {@link SupplierContact} entities.
 * <p>
 * Provides queries to find and delete contacts associated with a supplier.
 * </p>
 */
public interface SupplierContactRepository extends R2dbcRepository<SupplierContact, UUID> {

    /**
     * Finds all contacts belonging to the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @return a {@link Flux} of {@link SupplierContact} entities
     */
    Flux<SupplierContact> findAllBySupplierId(UUID supplierId);

    /**
     * Deletes all contacts associated with the given supplier.
     *
     * @param supplierId the supplier's unique identifier
     * @return a {@link Mono} that completes when deletion is done
     */
    Mono<Void> deleteAllBySupplierId(UUID supplierId);
}
