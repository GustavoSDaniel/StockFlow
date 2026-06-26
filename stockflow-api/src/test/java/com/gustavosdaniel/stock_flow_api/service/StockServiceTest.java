package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;
import com.gustavosdaniel.stock_flow_api.domain.mapping.StockMapper;
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
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
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

}