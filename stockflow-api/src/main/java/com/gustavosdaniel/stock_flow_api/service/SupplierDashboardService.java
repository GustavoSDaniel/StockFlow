package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class SupplierDashboardService {

    private final SuppliersRepository suppliersRepository;
    private final DashboardCacheManager dashboardCacheManager;


    public SupplierDashboardService(SuppliersRepository suppliersRepository, DashboardCacheManager dashboardCacheManager) {
        this.suppliersRepository = suppliersRepository;
        this.dashboardCacheManager = dashboardCacheManager;
    }

    public Mono<DashboardSupplierResponse> getSupplierDashboard(){

        return dashboardCacheManager.getOrCompute(
                DashboardCacheKeys.SUPPLIER,
                suppliersRepository.getDashboardSupplierStats()
                        .collectList()
                        .map(DashboardSupplierResponse::new)
        );
    }
}
