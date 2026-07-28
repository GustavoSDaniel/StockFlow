package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard.DashboardStockResponse;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheKeys;
import com.gustavosdaniel.stock_flow_api.util.cache.DashboardCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

/**
 * Service that assembles aggregated stock-level data (status counts, top 10 lowest/highest)
 * for the Stock dashboard, with caching support.
 */
@Service
public class StockDashboardService {

    private final Logger log = LoggerFactory.getLogger(StockDashboardService.class);
    private final StockRepository stockRepository;
    private final DashboardCacheManager cacheManager;


    public StockDashboardService(StockRepository stockRepository, DashboardCacheManager cacheManager) {
        this.stockRepository = stockRepository;
        this.cacheManager = cacheManager;
    }

    /**
     * Builds the stock dashboard response containing stock status distribution
     * and the top 10 lowest and highest stock entries.
     *
     * @return a Mono emitting the cached or freshly computed dashboard response
     */
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

                        .doFirst(() -> log.info("Acessando dashboard de Stocks"))
                        .doOnNext(movements ->
                                log.info("Dashboard Acessado com sucesso"))
        );
    }
}
