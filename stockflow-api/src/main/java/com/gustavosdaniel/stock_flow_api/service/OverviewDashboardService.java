package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.repository.*;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
public class OverviewDashboardService {

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final NotificationRepository notificationRepository;
    private final CategoryRepository categoryRepository;
    private final SuppliersRepository suppliersRepository;
    private final DashboardCacheManager cacheManager;


    public OverviewDashboardService(ProductRepository productRepository, StockRepository stockRepository, NotificationRepository notificationRepository, CategoryRepository categoryRepository, SuppliersRepository suppliersRepository, DashboardCacheManager cacheManager) {
        this.productRepository = productRepository;
        this.stockRepository = stockRepository;
        this.notificationRepository = notificationRepository;
        this.categoryRepository = categoryRepository;
        this.suppliersRepository = suppliersRepository;
        this.cacheManager = cacheManager;
    }

    Mono<DashboardOverviewResponse> getOverviewDashboard(){

        return cacheManager.getOrCompute(
                DashboardCacheKeys.OVERVIEW,
                Mono.zip(
                        productRepository.getDashboardProductStats(),
                        stockRepository.getDashboardFinancialStats(),
                        categoryRepository.count(),
                        suppliersRepository.count(),
                        notificationRepository.countByNotificationPriorityInAndResolvedFalse(
                                List.of(NotificationPriority.CRITICAL, NotificationPriority.HIGH))
                ).map(tuple -> new DashboardOverviewResponse(

                        tuple.getT1(),
                        tuple.getT2(),
                        tuple.getT3().intValue(),
                        tuple.getT4().intValue(),
                        tuple.getT5().intValue()
                ))
        );

    }
}
