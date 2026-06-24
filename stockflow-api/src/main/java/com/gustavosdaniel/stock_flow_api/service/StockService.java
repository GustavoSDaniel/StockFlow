package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockUpdate;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;
import com.gustavosdaniel.stock_flow_api.domain.mapping.StockMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.ProductNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.StockNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.InventoryMovementRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final StockMapper stockMapper;
    private final ProductRepository productRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final Logger log = LoggerFactory.getLogger(StockService.class);

    public StockService(StockRepository stockRepository, StockMapper stockMapper, ProductRepository productRepository, InventoryMovementRepository inventoryMovementRepository) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
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
    public Mono<StockResponse> getStockById(UUID id){

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock ->
                    productRepository.findById(stock.getProductId())
                            .map(product -> stockMapper.toStockResponse(stock, product))
                )
                .doFirst(() -> log.info("Buscando stock pelo ID: {}", id))
                .doOnNext(response -> log.info("Estoque encontrado pelo ID: {}", id));
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

        return toStockSummaryPage(
                stockRepository.findAllBy(pageable),
                stockRepository.count(),
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os estoques"))
                .doOnNext(page ->
                        log.info("Total de estoques: {}", page.getTotalElements()));
    }

    @Transactional
    public Mono<Void> registerEntry(UUID id , InventoryMovementRequest request){

        validateEntry(request.movementType(), request.movementReason());

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {

                    int quantityBefore = stock.getCurrentQuantity();
                    stock.addStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement movement = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                movement.registerStockLowEvent(savedStock);
                                return inventoryMovementRepository.save(movement)
                                        .doOnSuccess(m -> {
                                            if (!movement.getDomainEvents().isEmpty())
                                                log.info("Eventos de dominio registrados para o stock: {}",
                                                        id);

                                            movement.clearDomainEvent();
                                        });
                            });
                })
                .doFirst(() ->
                        log.info("Iniciando o processo de adicionar quantidade no estoque"))
                .doOnSuccess(v -> log.info("Stock: {} recebeu {} unidades",
                        id, request.quantity()))
                .then();
    }

    @Transactional
    public Mono<Void> registerExit(UUID id, InventoryMovementRequest request){

        validateExit(request.movementType(), request.movementReason());

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {
                    int quantityBefore = stock.getCurrentQuantity();
                    stock.removeStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement movement = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                movement.registerStockLowEvent(savedStock);

                                return inventoryMovementRepository.save(movement)
                                        .doOnSuccess(m -> {
                                            if (!movement.getDomainEvents().isEmpty())
                                                log.info("Eventos de dominio registrados para o stock: {}",
                                                        id);
                                            movement.clearDomainEvent();
                                        });
                            });
                })
                .doFirst(() -> log.info("Removendo saldo do estoque: {}", id))
                .doOnSuccess(v -> log.info("Stock: {} remove {}", id, request.quantity()))
                .then();
    }

    @Transactional
    public Mono<Void> adjustStock(UUID id, InventoryMovementRequest request){

        validateAjust(request.movementType(), request.movementReason());

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {

                    int quantityBefore = stock.getCurrentQuantity();
                    stock.adjustStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement moviment = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                moviment.registerStockLowEvent(savedStock);
                                return inventoryMovementRepository.save(moviment)
                                        .doOnSuccess(m -> {
                                            if (!moviment.getDomainEvents().isEmpty())
                                                log.info("Eventos de dominio registrados para o stock: {}",
                                                        id);
                                            moviment.clearDomainEvent();
                                        });
                            });
                })
                .doFirst(() -> log.warn("Ajustando a quantidade do estoque"))
                .doOnSuccess(v -> log.info("Estoque ajustado para: {} unidades",
                        request.quantity()))
                .then();
    }

    @Transactional(readOnly = true)
    public Mono<Page<StockSummaryResponse>> findOutOfStock(Pageable pageable){

        return toStockSummaryPage(
                stockRepository.findOutOfStock(pageable.getPageSize(), pageable.getOffset()),
                stockRepository.countOutOfStock(),
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os estoques que se encontram zerados"))
                .doOnNext(page ->
                        log.info("Foram encontrados {} estoques zerados", page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<StockSummaryResponse>> findLowStockProducts(Pageable pageable){

        return toStockSummaryPage(
                stockRepository.findLowStock(pageable.getPageSize(), pageable.getOffset()),
                stockRepository.countLowStock(),
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os estoques baixo com seu respectivo produto"))
                .doOnNext(page ->
                        log.info("Encontrados {} produtos com estoque baixo", page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<StockSummaryResponse>> findOverStock(Pageable pageable){

        return toStockSummaryPage(
                stockRepository.findOverStock(pageable.getPageSize(), pageable.getOffset()),
                stockRepository.countOverStock(),
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os estoque que estão acima do Limite"))
                .doOnNext(page ->
                        log.info("Todos os estoques {} encontrado acima da quantidade maxima",
                                page.getTotalElements()));
    }

    @Transactional
    public Mono<StockResponse> updateStock(UUID id, StockUpdate request){

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> {
                    stockMapper.applyUpdate(stock, request);
                    stock.validate();
                    return stockRepository.save(stock);
                })
                .flatMap(stock -> productRepository.findById(stock.getProductId())
                        .map(product -> stockMapper.toStockResponse(stock, product)))
                .doFirst(() -> log.info("Iniciando processo para atualizar as informações do estoque"))
                .doOnNext(response -> log.info("Estoque atualizado com sucesso: {}", id));
    }

    private Mono<Map<UUID, Product>> resolveProductMap(List<UUID> productIds) {
        if (productIds.isEmpty()) return Mono.just(Map.of());

        List<UUID> distinctIds = productIds.stream()
                .distinct()
                .collect(Collectors.toList());

        return productRepository.findAllById(distinctIds)
                .collectMap(Product::getId, Function.identity());
    }

    private Mono<Page<StockSummaryResponse>> toStockSummaryPage(
            Flux<Stock> stockFlux,
            Mono<Long> countMono,
            Pageable pageable
    ){
        Mono<List<Stock>> stocksListMono = stockFlux.collectList().cache();

        Mono<Map<UUID, Product>> productMapMono = stocksListMono
                .map(stocks -> stocks.stream()
                        .map(Stock::getProductId)
                        .collect(Collectors.toList()))
                .flatMap(this::resolveProductMap);

        return Mono.zip(stocksListMono, productMapMono, countMono)
                .map(tuple -> {
                            List<Stock> stocks = tuple.getT1();
                            Map<UUID, Product> productMap = tuple.getT2();
                            Long total = tuple.getT3();

                            List<StockSummaryResponse> responses = stocks.stream()
                                    .map(stock -> stockMapper.toStockSummaryResponse(stock,
                                            productMap.get(stock.getProductId())
                                    ))
                                    .collect(Collectors.toList());

                            return new PageImpl<>(responses, pageable, total);
                        });
    }

    private void validateStockBelongsToProduct(Stock stock, Product product){

        if (!stock.getProductId().equals(product.getId()))
            throw new  BusinessRuleException(
                    "O estoque informado não pertence ao produto fornecido na requisição."
            );
    }

    private void validateEntry(MovementType type, MovementReason reason){

        if (type != MovementType.ENTRY && type != MovementType.RETURN)
            throw new BusinessRuleException("Tipo inválido para entrada. Use ENTRY ou RETURN.");

        type.validateReason(reason);
    }

    private void validateExit(MovementType type, MovementReason reason){

        if (type != MovementType.EXIT && type != MovementType.TRANSFER)
            throw new BusinessRuleException("Tipo inválido para saída. Use EXIT ou TRANSFER.");
        type.validateReason(reason);
    }

    private void validateAjust(MovementType type, MovementReason reason){

        if (type != MovementType.ADJUSTMENT)
            throw new BusinessRuleException("Tipo inválido para ajuste. Use ADJUSTMENT.");
        type.validateReason(reason);
    }

}
