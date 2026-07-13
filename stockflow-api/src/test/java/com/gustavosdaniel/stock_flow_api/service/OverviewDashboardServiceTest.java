package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.repository.*;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

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

        DashboardOverviewResponse.ProductStats productStats =
                new DashboardOverviewResponse.ProductStats(
                    total, active, inactive, discontinued
                );

        DashboardOverviewResponse.FinancialStats financialStats =
                new DashboardOverviewResponse.FinancialStats(
                    totalStockValue, potentialSalesValue, averageMarginPercentage
                );
    }

}