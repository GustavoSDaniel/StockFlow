package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SupplierDashboardServiceTest {

    @Mock
    private SuppliersRepository suppliersRepository;

    @Mock
    private DashboardCacheManager dashboardCacheManager;

    @InjectMocks
    private SupplierDashboardService supplierDashboardService;

    @Test
    @DisplayName("Should get supplier dashboard successfully")
    void supplierDashboard(){

        UUID supplierId = UUID.randomUUID();
        String supplierName = "Fornecedor de celular";
        Long totalProducts = 13L;
        BigDecimal totalStockValue = BigDecimal.valueOf(76.530);

        DashboardSupplierResponse.SupplierDashboardItem supplierDashboardItem =
                new DashboardSupplierResponse.SupplierDashboardItem(
                        supplierId, supplierName, totalProducts, totalStockValue);

        when(suppliersRepository.getDashboardSupplierStats()).thenReturn(Flux.just(supplierDashboardItem));
        when(dashboardCacheManager.getOrCompute(eq(DashboardCacheKeys.SUPPLIER), any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        Mono<DashboardSupplierResponse> output = supplierDashboardService.getSupplierDashboard();

        StepVerifier.create(output)
                .assertNext(result -> {
                    assertNotNull(result.suppliers());
                    assertEquals(1, result.suppliers().size());
                })
                .verifyComplete();

        verify(suppliersRepository, times(1)).getDashboardSupplierStats();
    }

}