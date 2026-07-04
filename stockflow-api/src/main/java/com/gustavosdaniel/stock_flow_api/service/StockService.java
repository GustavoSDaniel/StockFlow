package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.*;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
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
import com.gustavosdaniel.stock_flow_api.messaging.event.StockEventPublisher;
import com.gustavosdaniel.stock_flow_api.repository.InventoryMovementRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
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
    private final StockEventPublisher stockEventPublisher;
    private final Logger log = LoggerFactory.getLogger(StockService.class);

    public StockService(StockRepository stockRepository, StockMapper stockMapper,
                        ProductRepository productRepository,
                        InventoryMovementRepository inventoryMovementRepository,
                        StockEventPublisher stockEventPublisher) {
        this.stockRepository = stockRepository;
        this.stockMapper = stockMapper;
        this.productRepository = productRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.stockEventPublisher = stockEventPublisher;
    }

    @Transactional
    public Mono<StockResponse> createStock(UUID productId, StockRequest request){

        Mono<Product> productMono = productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()));

        Mono<Boolean> stockExistsMono = stockRepository
                .existsByProductIdAndWarehouseId(productId, request.warehouseId());

        return Mono.zip(productMono, stockExistsMono)
                .flatMap(tuple -> {

                    Product product = tuple.getT1();
                    boolean stockExists = tuple.getT2();

                    if (stockExists) return Mono.error(
                            new BusinessRuleException(
                                "Já existe um estoque vinculado para esse produto neste armazém"));

                    Stock newStock = stockMapper.toStock(product.getId(), request);

                    newStock.validate();

                    return stockRepository.save(newStock)
                            .map(stock -> stockMapper.toStockResponse(stock, product));

                })
                .doFirst(() -> log.info("Vinculando stock para o produto: {} no armazém: {}",
                        productId, request.warehouseId()))
                .doOnNext(response -> log.info("Stock criado para o produto: {} no armazém: {}",
                        productId, request.warehouseId()));
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
    public Flux<StockResponse> getStockByProductId(UUID productId){

        return productRepository.findById(productId)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMapMany(product ->
                    stockRepository.findAllStockByProductId(productId)
                        .switchIfEmpty(Mono.error(new StockNotFoundException()))
                        .map(stock -> stockMapper.toStockResponse(stock, product))
                )
                .doFirst(() -> log.info("Buscando estoques pelo ID do produto: {}", productId))
                .doOnNext(response -> log.info("Estoques encontrados: armazém = {}, produto = {}",
                        response.warehouseId(), productId));
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

    @Transactional(readOnly = true)
    public Mono<Page<InventoryMovementResponse>> getMovementHistory(UUID stockId, Pageable pageable){

        return PageUtils.toPage(
                inventoryMovementRepository.findAllByStockId(stockId, pageable),
                inventoryMovementRepository.countByStockId(stockId),
                stockMapper::toInventoryMovementResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando histórico de movimentações para o estoque: {}",
                        stockId))
                .doOnNext(page -> log.info("Histórico encontrado. Total de registros: {}",
                        page.getTotalElements()));
    }

    @Transactional
    public Mono<Void> registerEntry(UUID id, InventoryMovementRequest request){

        validateEntry(request.movementType(), request.movementReason());

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> validateProductActive(stock).thenReturn(stock))
                .flatMap(stock -> {

                    int quantityBefore = stock.getCurrentQuantity();
                    stock.addStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement movement = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                movement.evaluateAndRegisterAlerts(savedStock);
                                return inventoryMovementRepository.save(movement)
                                        .flatMap(m ->
                                                stockEventPublisher.publish(m).thenReturn(m) )
                                        .doOnSuccess(m ->
                                                m.clearDomainEvent());
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
                .flatMap(stock -> validateProductActive(stock).thenReturn(stock))
                .flatMap(stock -> {
                    int quantityBefore = stock.getCurrentQuantity();
                    stock.removeStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement movement = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                movement.evaluateAndRegisterAlerts(savedStock);

                                return inventoryMovementRepository.save(movement)
                                        .flatMap(m ->
                                                stockEventPublisher.publish(m).thenReturn(m))
                                        .doOnSuccess(m -> m.clearDomainEvent());
                            });
                })
                .doFirst(() -> log.info("Removendo saldo do estoque: {}, em uma operação de: {}", id,
                        request.movementReason()))
                .doOnSuccess(v -> log.info("Stock: {} removeu quantidade de {}",
                        id, request.quantity()))
                .then();
    }

    @Transactional
    public Mono<Void> adjustStock(UUID id, InventoryMovementRequest request){

        validateAjust(request.movementType(), request.movementReason());

        return stockRepository.findById(id)
                .switchIfEmpty(Mono.error(new StockNotFoundException()))
                .flatMap(stock -> validateProductActive(stock).thenReturn(stock))
                .flatMap(stock -> {

                    int quantityBefore = stock.getCurrentQuantity();
                    stock.adjustStock(request.quantity());
                    int quantityAfter = stock.getCurrentQuantity();
                    return stockRepository.save(stock)
                            .flatMap(savedStock -> {
                                InventoryMovement moment = stockMapper.toInventoryMovement(
                                        request, savedStock, quantityBefore, quantityAfter
                                );
                                moment.evaluateAndRegisterAlerts(savedStock);
                                return inventoryMovementRepository.save(moment)
                                        .flatMap(m ->
                                                stockEventPublisher.publish(m).thenReturn(m))
                                        .doOnSuccess(m -> m.clearDomainEvent());
                            });
                })
                .doFirst(() -> log.warn("Ajustando a quantidade do estoque"))
                .doOnSuccess(v -> log.info("Ajustando o estoque que estava errado na quantidade de: {} unidades",
                        request.quantity()))
                .then();
    }

    @Transactional
    public Mono<Void> transferStock(UUID productId, TransferRequest request){

        Mono<Stock> stockSourceMono = stockRepository
                .findByProductIdAndWarehouseId(productId, request.sourceWarehouseId())
                .switchIfEmpty(Mono.error(new StockNotFoundException()));

        Mono<Stock> stockTargetMono = stockRepository
                .findByProductIdAndWarehouseId(productId, request.targetWarehouseId())
                .switchIfEmpty(Mono.error(new StockNotFoundException()));

        return Mono.zip(stockSourceMono, stockTargetMono)
                .flatMap(tuple -> {

                    Stock sourceStock = tuple.getT1();
                    Stock targetStock = tuple.getT2();

                    int sourceQtyBefore = sourceStock.getCurrentQuantity();
                    int targetQtyBefore = targetStock.getCurrentQuantity();

                    sourceStock.removeStock(request.quantity());
                    targetStock.addStock(request.quantity());

                    int sourceQtyAfter = sourceStock.getCurrentQuantity();
                    int targetQtyAfter = targetStock.getCurrentQuantity();

                    InventoryMovement sourceMovement = InventoryMovement.createTransfer(

                            productId,
                            sourceStock.getId(),
                            MovementType.EXIT,
                            request.quantity(),
                            sourceQtyBefore,
                            sourceQtyAfter,
                            request.referenceNumber(),
                            request.note()
                    );
                    sourceMovement.evaluateAndRegisterAlerts(sourceStock);

                    InventoryMovement targetMovement = InventoryMovement.createTransfer(

                            productId,
                            targetStock.getId(),
                            MovementType.ENTRY,
                            request.quantity(),
                            targetQtyBefore,
                            targetQtyAfter,
                            request.referenceNumber(),
                            request.note()
                    );

                    return stockRepository.saveAll(List.of(sourceStock, targetStock))
                            .thenMany(inventoryMovementRepository
                                    .saveAll(List.of(sourceMovement, targetMovement)))
                            .flatMap(savedMovement ->
                                stockEventPublisher.publish(savedMovement)
                                        .thenReturn(savedMovement)
                            )
                            .doOnNext(savedMovement -> {
                                savedMovement.clearDomainEvent();
                            })
                            .then();
                }).doFirst(() -> log.info("Transferindo {} unidades do produto {} de {} para {}",
                        request.quantity(), productId,
                        request.sourceWarehouseId(), request.targetWarehouseId()))
                .doOnSuccess(v -> log.info( "Transferência de {} unidades realizada com sucesso",
                        request.quantity()));
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

    private Mono<Product> validateProductActive(Stock stock) {
        return productRepository.findById(stock.getProductId())
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMap(product -> {
                    if (!product.isActive()) {
                        return Mono.error(new BusinessRuleException(
                            "Não é possível movimentar estoque de um produto inativo ou descontinuado"));
                    }
                    return Mono.just(product);
                });
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

    private void validateEntry(MovementType type, MovementReason reason){

        if (type != MovementType.ENTRY && type != MovementType.RETURN && type != MovementType.TRANSFER)
            throw new BusinessRuleException("Tipo inválido para entrada. Use ENTRY, RETURN ou TRANSFER.");

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
