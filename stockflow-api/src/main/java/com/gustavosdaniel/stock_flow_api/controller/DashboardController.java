package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.DashboardOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardMovementsResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardOverviewResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardSupplierResponse;
import com.gustavosdaniel.stock_flow_api.service.MovementsDashboardService;
import com.gustavosdaniel.stock_flow_api.service.OverviewDashboardService;
import com.gustavosdaniel.stock_flow_api.service.StockDashboardService;
import com.gustavosdaniel.stock_flow_api.service.SupplierDashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * REST controller for dashboard metrics at {@code /api/v1/dashboards}.
 * Exposes endpoints for overview, stock, movements, and supplier dashboards.
 */
@RestController
@RequestMapping("/api/v1/dashboards")
public class DashboardController implements DashboardOpenApi {

    private final OverviewDashboardService overviewDashboardService;
    private final MovementsDashboardService movementsDashboardService;
    private final StockDashboardService stockDashboardService;
    private final SupplierDashboardService supplierDashboardService;

    public DashboardController(OverviewDashboardService overviewDashboardService, MovementsDashboardService movementsDashboardService, StockDashboardService stockDashboardService, SupplierDashboardService supplierDashboardService) {
        this.overviewDashboardService = overviewDashboardService;
        this.movementsDashboardService = movementsDashboardService;
        this.stockDashboardService = stockDashboardService;
        this.supplierDashboardService = supplierDashboardService;
    }

    @GetMapping("/overview")
    public Mono<ResponseEntity<DashboardOverviewResponse>> getDashboardOverview(){

        return overviewDashboardService.getOverviewDashboard().map(ResponseEntity::ok);
    }

    @GetMapping("/stocks")
    public Mono<ResponseEntity<DashboardStockResponse>> getDashboardStock(){

        return stockDashboardService.getStockDashboard().map(ResponseEntity::ok);
    }

    @GetMapping("/movements")
    public Mono<ResponseEntity<DashboardMovementsResponse>> getDashboardMovements(){

        return movementsDashboardService.getMovementsDashboard().map(ResponseEntity::ok);
    }

    @GetMapping("/suppliers")
    public Mono<ResponseEntity<DashboardSupplierResponse>> getDashboardSupplier(){

        return supplierDashboardService.getSupplierDashboard().map(ResponseEntity::ok);
    }
}
