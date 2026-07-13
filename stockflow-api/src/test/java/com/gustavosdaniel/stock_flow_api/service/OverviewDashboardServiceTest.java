package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.NotificationPriority;
import com.gustavosdaniel.stock_flow_api.repository.*;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.checkerframework.checker.units.qual.N;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class OverviewDashboardServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SuppliersRepository suppliersRepository;

    @Mock
    private DashboardCacheManager cacheManager;

    @InjectMocks
    private OverviewDashboardService overviewDashboardService;

    @Test
    @DisplayName("Should with sucesso dashboard Overview")
    void getDashboardOverview(){

        Long total = 256L;
        Long active = 196L;
        Long inactive = 26L;
        Long discontinued = 34L;

        BigDecimal totalStockValue = BigDecimal.valueOf(356000);
        BigDecimal potentialSalesValue = BigDecimal.valueOf(462800);
        BigDecimal averageMarginPercentage = BigDecimal.valueOf(106800);

        Long totalSuppliers = 22L;
        Long totalCategories = 13L;
        Long totalNotifications = 67L;

        NotificationPriority notificationPriority = NotificationPriority.CRITICAL;
        NotificationPriority notificationPriority1 = NotificationPriority.HIGH;

        List<NotificationPriority> priorities = List.of(notificationPriority, notificationPriority1);

        DashboardOverviewResponse.ProductStats productStats =
                new DashboardOverviewResponse.ProductStats(
                    total, active, inactive, discontinued
                );

        DashboardOverviewResponse.FinancialStats financialStats =
                new DashboardOverviewResponse.FinancialStats(
                    totalStockValue, potentialSalesValue, averageMarginPercentage
                );

        when(productRepository.getDashboardProductStats()).thenReturn(Mono.just(productStats));
        when(stockRepository.getDashboardFinancialStats()).thenReturn(Mono.just(financialStats));
        when(suppliersRepository.count()).thenReturn(Mono.just(totalSuppliers));
        when(categoryRepository.count()).thenReturn(Mono.just(totalCategories));
        when(notificationRepository.countByNotificationPriorityInAndResolvedFalse(priorities))
                .thenReturn(Mono.just(totalNotifications));
        when(cacheManager.getOrCompute(eq(DashboardCacheKeys.OVERVIEW), any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        Mono<DashboardOverviewResponse> output = overviewDashboardService.getOverviewDashboard();

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertNotNull(resultado.products());
                    assertNotNull(resultado.financials());
                    assertNotNull(resultado.totalSuppliers());
                    assertNotNull(resultado.totalCategories());
                    assertNotNull(resultado.totalNotifications());
                })
                .verifyComplete();

        verify(productRepository, times(1)).getDashboardProductStats();
        verify(stockRepository, times(1)).getDashboardFinancialStats();
        verify(suppliersRepository).count();
        verify(categoryRepository).count();
        verify(notificationRepository).countByNotificationPriorityInAndResolvedFalse(priorities);

    }

}