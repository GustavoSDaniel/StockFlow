package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class StockDashboardService {

    private final StockRepository stockRepository;
    private final DashboardCacheManager cacheManager;


    public StockDashboardService(StockRepository stockRepository, DashboardCacheManager cacheManager) {
        this.stockRepository = stockRepository;
        this.cacheManager = cacheManager;
    }

    public Mono<DashboardStockResponse> getStockDashboard(){

        return cacheManager.getOrCompute(
                DashboardCacheKeys.STOCK,
                Mono.zip(
                        stockRepository.getDashboardStockStatusCounts(),
                        stockRepository.getTop10LowestStock().collectList(),
                                stockRepository.getTop10HighestStock().collectList()
                )
                        .map(tuple -> new DashboardStockResponse(

                                tuple.getT1(),
                                tuple.getT2(),
                                tuple.getT3()
                        ))
        );
    }
}
