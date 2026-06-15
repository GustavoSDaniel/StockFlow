package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.mapping.ProductMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.SupplierNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.SkuGenerator;
import org.apache.commons.lang3.function.Suppliers;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import org.slf4j.Logger;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

@Service
public class ProductService {

    private final Logger log = LoggerFactory.getLogger(ProductService.class);
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;
    private final SuppliersRepository suppliersRepository;

    public ProductService(ProductRepository productRepository, ProductMapper productMapper, CategoryRepository categoryRepository, SuppliersRepository suppliersRepository) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
        this.categoryRepository = categoryRepository;
        this.suppliersRepository = suppliersRepository;
    }

    @Transactional
    public Mono<ProductResponse> createProduct(ProductRequest request){

        Mono<Category> categoryMono = categoryRepository.findById(request.categoryId())
                .switchIfEmpty(Mono.error(new CategoryNotFoundException()));

        Mono<Supplier> supplierMono = suppliersRepository.findById(request.supplierId())
                .switchIfEmpty(Mono.error(new SupplierNotFoundException()));

        Mono<Boolean> existsNameMono = productRepository.existsByName(request.name());

        return Mono.zip(categoryMono, supplierMono, existsNameMono)
                .flatMap(tuple -> {

                    Category category = tuple.getT1();
                    Supplier supplier = tuple.getT2();
                    Boolean nameAlreadyExists = tuple.getT3();

                    if (nameAlreadyExists)
                        return Mono.error(new BusinessRuleException(
                                "O nome deste produto já está em uso no sistema."
                        ));

                    return generateUniqueSku(
                            category.getName(),
                            supplier.getTradeName(),
                            request.name(),
                            1
                    );
                })
                .flatMap(sku -> {

                    Product newProduct = productMapper.toProduct(request, sku);

                    return productRepository.save(newProduct);

                })
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Iniciando processo para criar produto"))
                .doOnNext(response ->
                        log.info("Produto: {} criado com SKU: {}", response.name(), response.sku()));
    }

    private Mono<String> generateUniqueSku(
            String categoryName,
            String supplierName,
            String productName,
            int attempt
    ){

        if (attempt > 5) {
            return Mono.error(new BusinessRuleException(
                    "Falha ao gerar SKU único após várias tentativas. Verifique o cadastro."));
        }

        return Mono.defer(() -> {

            String sku = SkuGenerator.generate(categoryName, supplierName, productName);

            return productRepository.existsBySku(sku)
                    .flatMap(exists -> {
                        if (exists){

                            log.warn("SKU {} já existe, gerando novo...", sku);
                            return generateUniqueSku(
                                    categoryName, supplierName, productName, attempt + 1);
                        }
                        return Mono.just(sku);
                    });
        });
    }
}
