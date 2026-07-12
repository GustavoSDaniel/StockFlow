package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.repository.InventoryMovementRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class MovementsDashboardServiceTest {

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private DashboardCacheManager dashboardCacheManager;

    @InjectMocks
    private MovementsDashboardService movementsDashboardService;

    @Test
    @DisplayName("Should with sucesso dashboard moviments")
    void getDashboardMovements(){

        Long totalMovementsToday = 50L;
        Long entriesThisMonth = 360L;
        Long exitsThisMonth = 7L;

        String movementType = "SALE";
        Long totalType = 30L;

        String movementReason = "SALE";
        Long totalReason = 30L;

        LocalDate date = LocalDate.of(2026, 5, 2);
        Long totalMovements = 40L;

        UUID productId = UUID.randomUUID();
        String productName = "CELULAR";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        Long totalQuantityMoved = 30L;

        DashboardMovementsResponse.MovementSummary summary =
                new DashboardMovementsResponse.MovementSummary(
                        totalMovementsToday, entriesThisMonth, exitsThisMonth);

        DashboardMovementsResponse.MovementTypeCount typeCount = new
                DashboardMovementsResponse.MovementTypeCount(
                movementType, totalType
        );

        DashboardMovementsResponse.MovementReasonCount reasonCount = new DashboardMovementsResponse.MovementReasonCount(
                movementReason, totalReason
        );

        DashboardMovementsResponse.DailyHistory dailyHistory = new DashboardMovementsResponse.DailyHistory(
                date, totalMovements
        );

        DashboardMovementsResponse.MostMovedProduct mostMovedProduct = new DashboardMovementsResponse.MostMovedProduct(
                productId, productName, sku, totalQuantityMoved
        );

        when(inventoryMovementRepository.getDashboardMovementSummary()).thenReturn(Mono.just(summary));
        when(inventoryMovementRepository.getDashboardMovementTypeCount()).thenReturn(Flux.just(typeCount));
        when(inventoryMovementRepository.getDashboardMovementReasonCount()).thenReturn(Flux.just(reasonCount));
        when(inventoryMovementRepository.getDashboardDailyHistory()).thenReturn(Flux.just(dailyHistory));
        when(inventoryMovementRepository.getDashboardMostMovedProduct()).thenReturn(Flux.just(mostMovedProduct));
        when(dashboardCacheManager.getOrCompute(eq(DashboardCacheKeys.MOVEMENTS), any(Mono.class)))
                .thenAnswer(invocation -> invocation.getArgument(1));


        Mono<DashboardMovementsResponse> output = movementsDashboardService.getMovementsDashboard();

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertEquals(50L, resultado.summary().totalMovementsToday());
                    assertEquals(360L, resultado.summary().entriesThisMonth());
                    assertEquals(productId, resultado.topMovedProducts().get(0).productId(), "Deve ser o mesmo ID do Produto");
                    assertEquals("SALE", resultado.movementsByType().get(0).movementType());
                })
                .verifyComplete();

        verify(inventoryMovementRepository, times(1)).getDashboardMovementSummary();
        verify(inventoryMovementRepository, times(1)).getDashboardMostMovedProduct();
    }

}