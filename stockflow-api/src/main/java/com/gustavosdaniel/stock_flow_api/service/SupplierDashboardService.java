package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service that aggregates supplier-level statistics for the Supplier dashboard,
 * with caching support.
 */
@Service
public class SupplierDashboardService {

    private final Logger log = LoggerFactory.getLogger(SupplierDashboardService.class);
    private final SuppliersRepository suppliersRepository;
    private final DashboardCacheManager dashboardCacheManager;


    public SupplierDashboardService(SuppliersRepository suppliersRepository, DashboardCacheManager dashboardCacheManager) {
        this.suppliersRepository = suppliersRepository;
        this.dashboardCacheManager = dashboardCacheManager;
    }

    /**
     * Builds the supplier dashboard response with per-supplier aggregate statistics.
     *
     * @return a Mono emitting the cached or freshly computed dashboard response
     */
    public Mono<DashboardSupplierResponse> getSupplierDashboard(){

        return dashboardCacheManager.getOrCompute(
                DashboardCacheKeys.SUPPLIER,
                suppliersRepository.getDashboardSupplierStats()
                        .collectList()
                        .map(DashboardSupplierResponse::new)
        )
                .doFirst(() -> log.info("Acessando dashboard de Supplier"))
                .doOnNext(movements ->
                        log.info("Dashboard Acessado com sucesso")
                );
    }
}
