package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
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

import java.util.UUID;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class StockDashboardServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private DashboardCacheManager dashboardCacheManager;

    @InjectMocks
    private StockDashboardService stockDashboardService;

    @Test
    @DisplayName("Should with sucesso dashboard stock")
    void getDashboardStock(){

        Long outOfStock = 3L;
        Long lowStock = 10L;
        Long reorderPoint = 26L;
        Long normal = 152L;
        Long overStocked = 14L;

        UUID productIdLow = UUID.randomUUID();
        String productNameLow = "Produto com estoque baixo";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        Integer currentQuantity = 13;
        Integer minimumQuantity = 6;
        StockStatus status = StockStatus.LOW;

        UUID productIdHigh = UUID.randomUUID();
        String productNameHigh = "Produto com estoque auto";
        StockStatus statusHigh = StockStatus.OUT_OF_STOCK;

        DashboardStockResponse.StockStatusCounts statusCounts =
                new DashboardStockResponse.StockStatusCounts(
                        outOfStock, lowStock, reorderPoint, normal, overStocked);

        DashboardStockResponse.ProductStockItem top10LowestStock =
                new DashboardStockResponse.ProductStockItem(
                        productIdLow, productNameLow, sku, currentQuantity, minimumQuantity, status
                );

        DashboardStockResponse.ProductStockItem top10HighestStock =
                new DashboardStockResponse.ProductStockItem(
                    productIdHigh, productNameHigh, sku, currentQuantity, minimumQuantity, statusHigh
                );

        when(stockRepository.getDashboardStockStatusCounts()).thenReturn(Mono.just(statusCounts));
        when(stockRepository.getTop10LowestStock()).thenReturn(Flux.just(top10LowestStock));
        when(stockRepository.getTop10HighestStock()).thenReturn(Flux.just(top10HighestStock));
        when(dashboardCacheManager.getOrCompute(eq(DashboardCacheKeys.STOCK), any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));

        Mono<DashboardStockResponse> output = stockDashboardService.getStockDashboard();

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertNotNull(resultado.statusCounts());
                    assertEquals(3L, resultado.statusCounts().outOfStock());
                    assertEquals(10L, resultado.statusCounts().lowStock());
                    assertEquals(26L, resultado.statusCounts().reorderPoint());
                    assertEquals(152L, resultado.statusCounts().normal());
                    assertEquals(14L, resultado.statusCounts().overStocked());

                    assertNotNull(resultado.top10LowestStock());
                    assertEquals(1, resultado.top10LowestStock().size());

                    assertNotNull(resultado.top10HighestStock());
                    assertEquals(1, resultado.top10HighestStock().size());

                })
                .verifyComplete();

        verify(stockRepository, times(1)).getDashboardStockStatusCounts();
        verify(stockRepository).getTop10LowestStock();
        verify(stockRepository).getTop10HighestStock();
    }

}