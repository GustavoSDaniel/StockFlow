package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.*;
import com.gustavosdaniel.stock_flow_api.domain.mapping.StockMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.InventoryMovement;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Stock;
import com.gustavosdaniel.stock_flow_api.messaging.event.StockEventPublisher;
import com.gustavosdaniel.stock_flow_api.repository.InventoryMovementRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.StockRepository;
import io.r2dbc.spi.Parameter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class StockServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @Mock
    private StockEventPublisher stockEventPublisher;

    @InjectMocks
    StockService stockService;

    @Test
    @DisplayName("Should with sucesso create stock")
    void create(){

        UUID categoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;
        ProductStatus status = ProductStatus.ACTIVE;

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);
        ReflectionTestUtils.setField(product, "status", status);

        UUID stockId = UUID.randomUUID();
        Integer currentQuantity = 100;
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";
        StockStatus stockStatus = StockStatus.NORMAL;

        StockRequest request = new StockRequest(minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);

        Stock newStock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);

        StockResponse response = new StockResponse(stockId, productId, productName, sku, currentQuantity,
                minimumQuantity, maximumQuantity, reorderPoint, reorderQuantity, stockStatus,
                location, warehouseId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(stockRepository.existsByProductIdAndWarehouseId(productId, warehouseId))
                .thenReturn(Mono.just(false));
        when(stockMapper.toStock(productId, request)).thenReturn(newStock);
        when(stockRepository.save(any(Stock.class))).thenReturn(Mono.just(newStock));
        when(stockMapper.toStockResponse(newStock, product)).thenReturn(response);

        Mono<StockResponse> output = stockService.createStock(productId, request);

        StepVerifier.create(output)
                .assertNext(resultado -> {

                    assertEquals(stockId, response.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(productRepository).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(stockRepository).existsByProductIdAndWarehouseId(productId, warehouseId);
        verify(stockMapper).toStock(productId, request);
        verify(stockRepository).save(any(Stock.class));
        verify(stockMapper).toStockResponse(newStock, product);
    }

    @Test
    @DisplayName("Should with sucesso stock by id")
    void findById(){

        UUID categoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        UUID stockId = UUID.randomUUID();
        Integer currentQuantity = 100;
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";
        StockStatus stockStatus = StockStatus.NORMAL;

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);

        StockResponse response = new StockResponse(stockId, productId, productName, sku, currentQuantity,
                minimumQuantity, maximumQuantity, reorderPoint, reorderQuantity, stockStatus,
                location, warehouseId);

        when(stockRepository.findById(stockId)).thenReturn(Mono.just(stock));
        when(productRepository.findById(stock.getProductId())).thenReturn(Mono.just(product));
        when(stockMapper.toStockResponse(stock, product)).thenReturn(response);

        Mono<StockResponse> output = stockService.getStockById(stockId);

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertEquals(stockId, response.id(), "O ID deve ser o mesmo");
                    assertEquals(productId, response.productId(), "ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(stockRepository).findById(stockId);
        verify(stockRepository, times(1)).findById(stockId);
        verify(productRepository).findById(stock.getProductId());
        verify(productRepository, times(1)).findById(stock.getProductId());
        verify(stockMapper).toStockResponse(stock, product);
    }

    @Test
    @DisplayName("Shoul with sucesso stock by Product ID")
    void findByStockWithProductId(){

        UUID categoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        UUID stockId = UUID.randomUUID();
        Integer currentQuantity = 100;
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";
        StockStatus stockStatus = StockStatus.NORMAL;

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);

        StockResponse response = new StockResponse(stockId, productId, productName, sku, currentQuantity,
                minimumQuantity, maximumQuantity, reorderPoint, reorderQuantity, stockStatus,
                location, warehouseId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(stockRepository.findAllStockByProductId(productId)).thenReturn(Flux.just(stock));
        when(stockMapper.toStockResponse(stock, product)).thenReturn(response);

        Flux<StockResponse> output = stockService.getStockByProductId(productId);

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertEquals(productId, stock.getProductId(), "O ID deve ser o mesmo");
                    assertEquals(stockId, response.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(productRepository).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(stockRepository).findAllStockByProductId(productId);
        verify(stockRepository, times(1)).findAllStockByProductId(productId);
        verify(stockMapper).toStockResponse(stock, product);
    }

    @Test
    @DisplayName("Should with ssucesso all stocks")
    void findAllStock(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId = UUID.randomUUID();
        UUID supplierId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        UUID stockId = UUID.randomUUID();
        Integer currentQuantity = 100;
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";
        StockStatus stockStatus = StockStatus.NORMAL;

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);

        StockSummaryResponse response = new StockSummaryResponse(stockId, productId, productName,
                sku, currentQuantity, stockStatus, location);

        when(productRepository.findAllById(List.of(productId))).thenReturn(Flux.just(product));
        when(stockMapper.toStockSummaryResponse(stock, product)).thenReturn(response);
        when(stockRepository.findAllBy(pageable)).thenReturn(Flux.just(stock));
        when(stockRepository.count()).thenReturn(Mono.just(1L));

        Mono<Page<StockSummaryResponse>> output = stockService.findAllStocks(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1 ,page.getTotalElements(), "A quantidade de elementos deve ser 1");
                })
                .verifyComplete();

        verify(stockRepository).findAllBy(pageable);
        verify(stockRepository).count();
    }

    @Test
    @DisplayName("Should with sucesso moviment historico")
    void getMovementHistory(){

        Pageable pageable = Pageable.unpaged();

        UUID productId = UUID.randomUUID();

        UUID stockId = UUID.randomUUID();
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";

        UUID movimentId = UUID.randomUUID();
        MovementType type = MovementType.ENTRY;
        Integer quantity = 70;
        Integer quantityBefore = 30;
        Integer quantityAfter = 100;
        MovementReason reason = MovementReason.PURCHASE;
        String referenceNumber = null;
        UUID supplierId = null;
        UUID customerId = null;
        String note = null;
        BigDecimal unitCost = BigDecimal.valueOf(2300);
        LocalDateTime createdAt = LocalDateTime.now();

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);

        InventoryMovement movement = new InventoryMovement(productId, stockId, type, quantity,
                quantityBefore, quantityAfter, reason, referenceNumber, supplierId, customerId, note, unitCost);
        ReflectionTestUtils.setField(movement, "id", movimentId);

        InventoryMovementResponse movementResponse = new InventoryMovementResponse(movimentId, createdAt, type,
                reason, quantity, quantityBefore, quantityAfter,
                referenceNumber, note, supplierId, customerId, unitCost);

        when(inventoryMovementRepository.findAllByStockId(stockId, pageable)).thenReturn(Flux.just(movement));
        when(inventoryMovementRepository.countByStockId(stockId)).thenReturn(Mono.just(1L));
        when(stockMapper.toInventoryMovementResponse(movement)).thenReturn(movementResponse);

        Mono<Page<InventoryMovementResponse>> output = stockService.getMovementHistory(stockId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(movimentId, movementResponse.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(inventoryMovementRepository).findAllByStockId(stockId, pageable);
        verify(inventoryMovementRepository, times(1)).findAllByStockId(stockId, pageable);
        verify(inventoryMovementRepository).countByStockId(stockId);
        verify(stockMapper).toInventoryMovementResponse(movement);
    }

    @Test
    @DisplayName("Should entry with sucesso")
    void entrySaldo(){

        UUID categoryId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        UUID stockId = UUID.randomUUID();
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";

        UUID movimentId = UUID.randomUUID();
        MovementType movementType = MovementType.RETURN;
        MovementReason movementReason = MovementReason.RETURN_CUSTOMER;
        Integer quantityBefore = 30;
        Integer quantity = 15;
        Integer quantityAfter = 45;
        String referenceNumber = "Devolução por conta de lote";
        UUID supplierId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String note = "Devolveu";
        BigDecimal unitCost = BigDecimal.valueOf(1800.00);

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        InventoryMovementRequest request = new InventoryMovementRequest(movementType,quantity,movementReason ,
                referenceNumber, supplierId, customerId, note, unitCost);

        InventoryMovement movement = new InventoryMovement(productId, stockId, movementType, quantity,
                quantityBefore, quantityAfter, movementReason, referenceNumber,
                supplierId, customerId, note, unitCost);
        ReflectionTestUtils.setField(movement, "id", movimentId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(stockRepository.findById(stockId)).thenReturn(Mono.just(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(Mono.just(stock));
        when(stockMapper.toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt()))
                .thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(Mono.just(movement));

        Mono<Void> output = stockService.registerEntry(stockId, request);

        StepVerifier.create(output).verifyComplete();

        verify(productRepository).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(stockRepository).findById(stockId);
        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository).save(any(Stock.class));
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockMapper).toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt());
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should exist with sucesso quantity")
    void existSaldo(){

        UUID categoryId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        UUID stockId = UUID.randomUUID();
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";

        UUID movimentId = UUID.randomUUID();
        MovementType movementType = MovementType.EXIT;
        MovementReason movementReason = MovementReason.SALE;
        Integer quantityBefore = 30;
        Integer quantity = 15;
        Integer quantityAfter = 15;
        String referenceNumber = "Devolução por conta de lote";
        UUID supplierId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String note = "Devolveu";
        BigDecimal unitCost = BigDecimal.valueOf(1800.00);

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);
        ReflectionTestUtils.setField(stock, "currentQuantity", quantityBefore);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        InventoryMovementRequest request = new InventoryMovementRequest(movementType,quantity,movementReason ,
                referenceNumber, supplierId, customerId, note, unitCost);

        InventoryMovement movement = new InventoryMovement(productId, stockId, movementType, quantity,
                quantityBefore, quantityAfter, movementReason, referenceNumber,
                supplierId, customerId, note, unitCost);
        ReflectionTestUtils.setField(movement, "id", movimentId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(stockRepository.findById(stockId)).thenReturn(Mono.just(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(Mono.just(stock));
        when(stockMapper.toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt()))
                .thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(Mono.just(movement));

        Mono<Void> output = stockService.registerExit(stockId, request);

        StepVerifier.create(output).verifyComplete();

        verify(productRepository).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(stockRepository).findById(stockId);
        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository).save(any(Stock.class));
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockMapper).toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt());
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Should ajuste estoque with sucesso")
    void adjustStock(){

        UUID categoryId = UUID.randomUUID();

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        UUID stockId = UUID.randomUUID();
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";

        UUID movimentId = UUID.randomUUID();
        MovementType movementType = MovementType.ADJUSTMENT;
        MovementReason movementReason = MovementReason.INVENTORY_COUNT;
        Integer quantityBefore = 15;
        Integer quantity = 15;
        Integer quantityAfter = 0;
        String referenceNumber = "Devolução por conta de lote";
        UUID supplierId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        String note = "Devolveu";
        BigDecimal unitCost = BigDecimal.valueOf(1800.00);

        Stock stock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(stock, "id", stockId);
        ReflectionTestUtils.setField(stock, "currentQuantity", quantityAfter);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        InventoryMovementRequest request = new InventoryMovementRequest(movementType,quantity,movementReason ,
                referenceNumber, supplierId, customerId, note, unitCost);

        InventoryMovement movement = new InventoryMovement(productId, stockId, movementType, quantity,
                quantityBefore, quantityAfter, movementReason, referenceNumber,
                supplierId, customerId, note, unitCost);
        ReflectionTestUtils.setField(movement, "id", movimentId);

        when(productRepository.findById(productId)).thenReturn(Mono.just(product));
        when(stockRepository.findById(stockId)).thenReturn(Mono.just(stock));
        when(stockRepository.save(any(Stock.class))).thenReturn(Mono.just(stock));
        when(stockMapper.toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt()))
                .thenReturn(movement);
        when(inventoryMovementRepository.save(any(InventoryMovement.class))).thenReturn(Mono.just(movement));

        Mono<Void> output = stockService.adjustStock(stockId, request);

        StepVerifier.create(output).verifyComplete();

        verify(productRepository).findById(productId);
        verify(productRepository, times(1)).findById(productId);
        verify(stockRepository).findById(stockId);
        verify(stockRepository, times(1)).findById(stockId);
        verify(stockRepository).save(any(Stock.class));
        verify(stockRepository, times(1)).save(any(Stock.class));
        verify(stockMapper).toInventoryMovement(eq(request), eq(stock), anyInt(), anyInt());
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));
    }

    @Test
    @DisplayName("Shoul with sucesso transfer")
    void transfer(){

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;

        Integer quantity = 10;

        Integer quantityBefore = 15;
        Integer quantityAfter = 5;

        Integer quantityAfter2 = 15;
        Integer quantityBefore2 = 5;

        String referenceNumber = "OC numero da transferencia";
        String note = "Transferencia realizada pela propia empresa";

        UUID stockId = UUID.randomUUID();
        Integer minimumQuantity = 20;
        Integer maximumQuantity = 150;
        Integer reorderPoint = 15;
        Integer reorderQuantity = 50;
        String location = "SP-CAPITAL";
        String warehouseId = "GALPAO-A";
        Integer currentQuantity = 20;
        MovementType movementType = MovementType.TRANSFER;

        UUID stockId2 = UUID.randomUUID();
        String warehouseId2 = "GALPAO-B";
        Integer currentQuantity2 = 20;


        Stock sourceStock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId);
        ReflectionTestUtils.setField(sourceStock, "id", stockId);
        ReflectionTestUtils.setField(sourceStock, "currentQuantity", currentQuantity);

        Stock targetStock = new Stock(productId, minimumQuantity, maximumQuantity, reorderPoint,
                reorderQuantity, location, warehouseId2);
        ReflectionTestUtils.setField(targetStock, "id", stockId);
        ReflectionTestUtils.setField(targetStock, "currentQuantity", currentQuantity2);

        InventoryMovement sourceMovement = InventoryMovement.createTransfer(
                productId, stockId, movementType, quantity, quantityBefore, quantityAfter, referenceNumber, note
        );

        InventoryMovement targetMovement = InventoryMovement.createTransfer(
                productId, stockId2, movementType, quantity,
                quantityBefore2, quantityAfter2, referenceNumber, note
        );
    }
}