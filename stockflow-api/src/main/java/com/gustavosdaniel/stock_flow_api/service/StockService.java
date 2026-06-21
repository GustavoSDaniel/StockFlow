package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.StockMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.ProductNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.StockNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final ProductRepository productRepository;
    private final Logger log = LoggerFactory.getLogger(StockService.class);

    public StockService(StockRepository stockRepository, StockMapper stockMapper, ProductRepository productRepository) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.productRepository = productRepository;
    }

    @Transactional
    public Mono<StockResponse> createStock(UUID productId, StockRequest request){

        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()));

        Mono<Boolean> stockExistsMono = stockRepository.existsByProductId(productId);

        return Mono.zip(productMono, stockExistsMono)
                .flatMap(tuple -> {

                    Product product = tuple.getT1();
                    boolean stockExists = tuple.getT2();

                    if (stockExists) return Mono.error(
                            new BusinessRuleException("Já existe um estoque vinculado para esse produto"));

                    Stock newStock = stockMapper.toStock(product.getId(), request);

                    newStock.validate();

                    return stockRepository.save(newStock)
                            .map(stock -> stockMapper.toStockResponse(stock, product));

                })
                .doFirst(() -> log.info("Vinculando stock para o produto: {}", productId))
                .doOnNext(response -> log.info("Stock criado para o produto> {}", productId));
    }

    @Transactional(readOnly = true)
    public Mono<StockResponse> getStockById(UUID id, UUID productId){

        Mono<Stock> stockMono = stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()));

        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()));

        return Mono.zip(stockMono, productMono)
                .map(tuple -> {
                    Stock stock = tuple.getT1();
                    Product product = tuple.getT2();
                    return stockMapper.toStockResponse(stock, product);
                })
                .doFirst(() -> log.info("Buscando stock pelo ID: {}", productId))
                .doOnNext(response -> log.info("Stoque encontrado pelo ID: {}", id));
    }

    @Transactional(readOnly = true)
    public Mono<StockResponse> getStockByProductId(UUID productId){

        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()));

        Mono<Stock> stockMono =stockRepository.findStockByProductId(productId)
                .switchIfEmpty(Mono.error(new StockNotFoundException()));

        return Mono.zip(productMono, stockMono)
                .map(tuple -> {
                    Product product = tuple.getT1();
                    Stock stock = tuple.getT2();

                    return stockMapper.toStockResponse(stock, product);
                })
                .doFirst(() -> log.info("Buscando estoque pelo ID do produto: {}", productId))
                .doOnNext(response -> log.info("Estoque encontrado pelo ID do produto: {}",
                        productId));
    }


}
