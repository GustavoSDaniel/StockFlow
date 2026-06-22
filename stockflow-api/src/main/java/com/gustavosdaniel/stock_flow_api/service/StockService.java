package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

                    validateStockBelongsToProduct(stock, product);

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

                    validateStockBelongsToProduct(stock, product);

                    return stockMapper.toStockResponse(stock, product);
                })
                .doFirst(() -> log.info("Buscando estoque pelo ID do produto: {}", productId))
                .doOnNext(response -> log.info("Estoque encontrado pelo ID do produto: {}",
                        productId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<StockSummaryResponse>> findAllStocks(Pageable pageable){

        Flux<StockSummaryResponse> stockSummaryResponseFlux = stockRepository.findAllBy(pageable)
                .flatMap(stock -> productRepository.findById(stock.getProductId())
                            .map(product -> stockMapper.toStockSummaryResponse(stock, product))
                );

        return PageUtils.toPage(
            stockSummaryResponseFlux,
                stockRepository.count(),
                response -> response,
                pageable
        );
    }

    @Transactional
    public Mono<Void> registerEntry(UUID id , StockMovementRequest request){

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {

                    stock.addStock(request.quantity());
                    return stockRepository.save(stock);
                })
                .doFirst(() ->
                        log.info("Iniciando o processo de adicionar quantidade no estoque"))
                .doOnSuccess(stock -> log.info("Stock: {} recebeu {}, ficando atualmente com: {}",
                        stock.getId(), request.quantity(), stock.getCurrentQuantity()))
                .then();
    }

    @Transactional
    public Mono<Void> registerExit(UUID id, StockMovementRequest request){

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {

                    stock.removeStock(request.quantity());
                    return stockRepository.save(stock);
                })
                .doFirst(() -> log.info("Removendo saldo do estoque: {}", id))
                .doOnSuccess(stock -> log.info("Saldo atual do stock é de: {}",
                        stock.getCurrentQuantity()))
                .then();
    }

    @Transactional
    public Mono<Void> adjustStock(UUID id, StockMovementRequest request){

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {

                    stock.adjustStock(request.quantity());
                    return stockRepository.save(stock);
                })
                .doFirst(() -> log.warn("Ajustando a quantidade do estoque"))
                .doOnSuccess(stock -> log.info("Estoque ajustado para: {}",
                        stock.getCurrentQuantity()))
                .then();
    }

    @Transactional
    public Mono<Page<StockSummaryResponse>> findLowStockProducts(UUID id,Pageable pageable){

        Flux<StockSummaryResponse> stockSummaryResponseFlux = stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock ->
                    productRepository.findById(stock.getProductId())
                            .map(product -> stockMapper.toStockSummaryResponse(stock, product))

                );

        return PageUtils.toPage(
                stockSummaryResponseFlux,

        )

    }

    private void validateStockBelongsToProduct(Stock stock, Product product){

        if (!stock.getProductId().equals(product.getId()))
            throw new  BusinessRuleException(
                    "O estoque informado não pertence ao produto fornecido na requisição."
            );
    }


}
