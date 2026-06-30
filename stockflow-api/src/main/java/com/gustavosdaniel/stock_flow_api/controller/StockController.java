package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.service.StockService;
import jakarta.validation.Valid;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/{productId}")
    public Mono<ResponseEntity<StockResponse>> createStock
            (@PathVariable UUID productId, @RequestBody @Valid StockRequest request)
    {
        return stockService.createStock(productId, request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<StockResponse>> getStockById(@PathVariable UUID id){

        return stockService.getStockById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/product/{productId}")
    public Flux<ResponseEntity<StockResponse>> getStockByProduct(@PathVariable UUID productId){

        return stockService.getStockByProductId(productId).map(ResponseEntity::ok);
    }

    @GetMapping
    public Mono<ResponseEntity<Page<StockSummaryResponse>>> allStocks(
            @ParameterObject @PageableDefault(size = 20)
            Pageable pageable)
    {
        return stockService.findAllStocks(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/stock/{stockId}")
    public Mono<ResponseEntity<Page<InventoryMovementResponse>>> getMovementHistory(
            @PathVariable UUID stockId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable)
    {
        return stockService.getMovementHistory(stockId, pageable).map(ResponseEntity::ok);
    }

    @PatchMapping("/entry/{id}")
    public Mono<ResponseEntity<Void>> registerEntry(@PathVariable UUID id,
                                                    @RequestBody @Valid InventoryMovementRequest request)
    {

        return stockService.registerEntry(id, request)
                .thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/exit/{id}")
    public Mono<ResponseEntity<Void>> registerExit (@PathVariable UUID id,
                                    @RequestBody @Valid InventoryMovementRequest request)
    {
        return stockService.registerExit(id, request).thenReturn(ResponseEntity.noContent().build());
    }
}
