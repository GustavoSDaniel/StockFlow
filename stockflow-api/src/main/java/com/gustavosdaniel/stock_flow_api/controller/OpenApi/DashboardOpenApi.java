package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@Tag(name = "Dashboard", description = "Métricas e relatórios")
public interface DashboardOpenApi {

    @Operation(summary = "Visão geral do dashboard",
            description = "Retorna métricas consolidadas da aplicação: total de produtos, estoque geral, movimentações recentes, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard overview",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardOverviewResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<DashboardOverviewResponse>> getDashboardOverview();

    @Operation(summary = "Dashboard de estoque",
            description = "Retorna indicadores de estoque: produtos com baixo estoque, fora de estoque, excesso, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard de estoque",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardStockResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<DashboardStockResponse>> getDashboardStock();

    @Operation(summary = "Dashboard de movimentações",
            description = "Retorna métricas de movimentações: entradas, saídas, ajustes recentes, tendências, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard de movimentações",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardMovementsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<DashboardMovementsResponse>> getDashboardMovements();

    @Operation(summary = "Dashboard de fornecedores",
            description = "Retorna indicadores de fornecedores: total, ativos, com mais produtos, etc.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dados do dashboard de fornecedores",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = DashboardSupplierResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<DashboardSupplierResponse>> getDashboardSupplier();
}
