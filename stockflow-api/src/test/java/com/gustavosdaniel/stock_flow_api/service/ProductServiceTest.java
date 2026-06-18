package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.enums.UnitMeasure;
import com.gustavosdaniel.stock_flow_api.domain.mapping.ProductMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
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
import java.util.UUID;
import static org.mockito.Mockito.*;


import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SuppliersRepository suppliersRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    @Test
    @DisplayName("Should create product with sucesso")
    void createProduct(){

        UUID categoryId = UUID.randomUUID();
        String categoryName = "Eletronicos";
        String description = "Produtos eletronicos";

        UUID supplierId = UUID.randomUUID();
        String supplierName = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;
        ProductStatus status = ProductStatus.ACTIVE;

        Category category = new Category(categoryName, description, null, false);
        ReflectionTestUtils.setField(category, "id", categoryId);

        Supplier supplier = new Supplier(supplierName, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        ProductRequest request = new ProductRequest(
                productName, null, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);

        Product newProduct = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(newProduct, "id", productId);

        ProductResponse response = new ProductResponse(
                productId, productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null, status);

        when(categoryRepository.findById(categoryId)).thenReturn(Mono.just(category));
        when(suppliersRepository.findById(supplierId)).thenReturn(Mono.just(supplier));
        when(productRepository.existsByNameAndStatus(productName, status)).thenReturn(Mono.just(false));
        when(productRepository.existsBySku(anyString())).thenReturn(Mono.just(false));
        when(productMapper.toProduct(eq(request), anyString())).thenReturn(newProduct);
        when(productRepository.save(any(Product.class))).thenReturn(Mono.just(newProduct));
        when(productMapper.toProductResponse(newProduct)).thenReturn(response);

        Mono<ProductResponse> output = productService.createProduct(request);

        StepVerifier.create(output)
                .assertNext(resultado -> {
                    assertEquals(productId, resultado.id(), "O ID deve ser o mesmo");
                })
                .verifyComplete();

        verify(categoryRepository).findById(categoryId);
        verify(suppliersRepository).findById(supplierId);
        verify(productRepository).existsByNameAndStatus(productName, status);
        verify(productMapper).toProduct(eq(request), anyString());
        verify(productRepository).save(any(Product.class));
        verify(productMapper).toProductResponse(newProduct);
    }

    @Test
    @DisplayName("Should All products")
    void allProducts(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId = UUID.randomUUID();
        String categoryName = "Eletronicos";
        String description = "Produtos eletronicos";

        UUID supplierId = UUID.randomUUID();
        String supplierName = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;
        ProductStatus status = ProductStatus.ACTIVE;

        UUID productId2 = UUID.randomUUID();
        String productName2 = "Celular";
        String sku2 = "ELET-NOME-CELU-3F3D-0002";
        BigDecimal costPrice2 = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice2 = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure2 = UnitMeasure.UN;
        ProductStatus status2 = ProductStatus.ACTIVE;

        Category category = new Category(categoryName, description, null, false);
        ReflectionTestUtils.setField(category, "id", categoryId);

        Supplier supplier = new Supplier(supplierName, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        Product product2 = new Product(productName2, null, sku2, categoryId, supplierId,
                costPrice2, salePrice2, unitMeasure2, null);
        ReflectionTestUtils.setField(product2, "id", productId2);

        ProductResponse response = new ProductResponse(
                productId, productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null, status);

        ProductResponse response2 = new ProductResponse(
                productId2, productName2, null, sku2, categoryId, supplierId,
                costPrice2, salePrice2, unitMeasure2, null, status2);

        when(productRepository.findAllBy(pageable)).thenReturn(Flux.just(product, product2));
        when(productRepository.count()).thenReturn(Mono.just(2L));
        when(productMapper.toProductResponse(product)).thenReturn(response);
        when(productMapper.toProductResponse(product2)).thenReturn(response2);

        Mono<Page<ProductResponse>> output = productService.allProducts(pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements(), "A pagina deve ter 2 itens");
                })
                .verifyComplete();

        verify(productRepository).findAllBy(pageable);
        verify(productRepository, times(1)).findAllBy(pageable);
        verify(productRepository).count();
        verify(productMapper).toProductResponse(product);
        verify(productMapper).toProductResponse(product2);
    }

    @Test
    @DisplayName("Should all product for status with sucesso")
    void allProductsAndStatus(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId = UUID.randomUUID();
        String categoryName = "Eletronicos";
        String description = "Produtos eletronicos";

        UUID supplierId = UUID.randomUUID();
        String supplierName = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;
        ProductStatus status = ProductStatus.ACTIVE;

        Category category = new Category(categoryName, description, null, false);
        ReflectionTestUtils.setField(category, "id", categoryId);

        Supplier supplier = new Supplier(supplierName, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        ProductResponse response = new ProductResponse(
                productId, productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null, status);

        when(productRepository.findAllByStatus(status, pageable)).thenReturn(Flux.just(product));
        when(productRepository.countByStatus(status)).thenReturn(Mono.just(1L));
        when(productMapper.toProductResponse(product)).thenReturn(response);

        Mono<Page<ProductResponse>> output = productService.findAllProductsByStatus(status, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(1, page.getTotalElements(), "Deve conter 1 elemento");
                })
                .verifyComplete();

        verify(productRepository).findAllByStatus(status, pageable);
        verify(productRepository, times(1)).findAllByStatus(status, pageable);
        verify(productRepository).countByStatus(status);
        verify(productMapper).toProductResponse(product);

    }

    @Test
    @DisplayName("Should product by category with sucesso")
    void findByProductByCategory(){

        Pageable pageable = Pageable.unpaged();

        UUID categoryId = UUID.randomUUID();
        String categoryName = "Eletronicos";
        String description = "Produtos eletronicos";

        UUID supplierId = UUID.randomUUID();
        String supplierName = "Fornecedor";
        String cnpj = "11122233344455";
        String tradeName = "Nome Fantasia";
        BigDecimal minOrderValue = BigDecimal.valueOf(300.00);

        UUID productId = UUID.randomUUID();
        String productName = "Celular";
        String sku = "ELET-NOME-CELU-3F3D-0001";
        BigDecimal costPrice = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure = UnitMeasure.UN;
        ProductStatus status = ProductStatus.ACTIVE;

        UUID productId2 = UUID.randomUUID();
        String productName2 = "Celular";
        String sku2 = "ELET-NOME-CELU-3F3D-0002";
        BigDecimal costPrice2 = BigDecimal.valueOf(1000.00);
        BigDecimal salePrice2 = BigDecimal.valueOf(3000.00);
        UnitMeasure unitMeasure2 = UnitMeasure.UN;
        ProductStatus status2 = ProductStatus.ACTIVE;

        Category category = new Category(categoryName, description, null, false);
        ReflectionTestUtils.setField(category, "id", categoryId);

        Supplier supplier = new Supplier(supplierName, cnpj, tradeName,
                null, minOrderValue, null);
        ReflectionTestUtils.setField(supplier, "id", supplierId);

        Product product = new Product(productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null);
        ReflectionTestUtils.setField(product, "id", productId);

        Product product2 = new Product(productName2, null, sku2, categoryId, supplierId,
                costPrice2, salePrice2, unitMeasure2, null);
        ReflectionTestUtils.setField(product2, "id", productId2);

        ProductResponse response = new ProductResponse(
                productId, productName, null, sku, categoryId, supplierId,
                costPrice, salePrice, unitMeasure, null, status);

        ProductResponse response2 = new ProductResponse(
                productId2, productName2, null, sku2, categoryId, supplierId,
                costPrice2, salePrice2, unitMeasure2, null, status2);

        when(categoryRepository.existsById(categoryId)).thenReturn(Mono.just(true));
        when(productRepository.findAllByCategoryId(categoryId, pageable)).thenReturn(Flux.just(product, product2));
        when(productRepository.countByCategoryId(categoryId)).thenReturn(Mono.just(2L));
        when(productMapper.toProductResponse(product)).thenReturn(response);
        when(productMapper.toProductResponse(product2)).thenReturn(response2);

        Mono<Page<ProductResponse>> output = productService.findProductByCategory(categoryId, pageable);

        StepVerifier.create(output)
                .assertNext(page -> {
                    assertEquals(2, page.getTotalElements(), "A pagina deve conter 2 elementos");
                })
                .verifyComplete();

        verify(categoryRepository).existsById(categoryId);
        verify(categoryRepository, times(1)).existsById(categoryId);
        verify(productRepository).findAllByCategoryId(categoryId, pageable);
        verify(productRepository, times(1)).findAllByCategoryId(categoryId, pageable);
        verify(productRepository).countByCategoryId(categoryId);
        verify(productMapper).toProductResponse(product);
        verify(productMapper).toProductResponse(product2);

    }
}