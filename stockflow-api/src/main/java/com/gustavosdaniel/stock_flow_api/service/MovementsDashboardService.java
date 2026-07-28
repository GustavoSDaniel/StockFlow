package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.repository.InventoryMovementRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service that assembles aggregated data for the Inventory Movements dashboard,
 * with results cached for performance.
 */
@Service
public class MovementsDashboardService {

    private final Logger log = LoggerFactory.getLogger(MovementsDashboardService.class);
    private final InventoryMovementRepository inventoryMovementRepository;
    private final DashboardCacheManager cacheManager;

    public MovementsDashboardService(InventoryMovementRepository inventoryMovementRepository, DashboardCacheManager cacheManager) {
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Builds the movements dashboard response containing movement summaries,
     * type/reason breakdowns, daily history, and most-moved products.
     *
     * @return a Mono emitting the cached or freshly computed dashboard response
     */
    public Mono<DashboardMovementsResponse> getMovementsDashboard(){

        return cacheManager.getOrCompute(
                DashboardCacheKeys.MOVEMENTS,
                Mono.zip(
                        inventoryMovementRepository.getDashboardMovementSummary(),
                        inventoryMovementRepository.getDashboardMovementTypeCount().collectList(),
                        inventoryMovementRepository.getDashboardMovementReasonCount().collectList(),
                        inventoryMovementRepository.getDashboardDailyHistory().collectList(),
                        inventoryMovementRepository.getDashboardMostMovedProduct().collectList()
                ).map(tuple ->
                        new DashboardMovementsResponse(

                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3(),
                        tuple.getT4(),
                        tuple.getT5()
                ))
                        .doFirst(() -> log.info("Acessando dashboard de Movements"))
                        .doOnNext(movements ->
                                log.info("Dashboard Acessado com sucesso"))
        );
    }
}
