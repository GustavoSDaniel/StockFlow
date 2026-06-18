package com.gustavosdaniel.stock_flow_api.service;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.domain.mapping.ProductMapper;
import com.gustavosdaniel.stock_flow_api.domain.po.Category;
import com.gustavosdaniel.stock_flow_api.domain.po.Product;
import com.gustavosdaniel.stock_flow_api.domain.po.Supplier;
import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import com.gustavosdaniel.stock_flow_api.exception.CategoryNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.ProductNotFoundException;
import com.gustavosdaniel.stock_flow_api.exception.SupplierNotFoundException;
import com.gustavosdaniel.stock_flow_api.repository.CategoryRepository;
import com.gustavosdaniel.stock_flow_api.repository.ProductRepository;
import com.gustavosdaniel.stock_flow_api.repository.SuppliersRepository;
import com.gustavosdaniel.stock_flow_api.util.PageUtils;
import com.gustavosdaniel.stock_flow_api.util.SkuGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.UUID;

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

        Mono<Boolean> existsNameMono = productRepository
                .existsByNameAndStatus(request.name(), ProductStatus.ACTIVE);

        return Mono.zip(categoryMono, supplierMono, existsNameMono)
                .flatMap(tuple -> {

                    Category category = tuple.getT1();
                    Supplier supplier = tuple.getT2();
                    Boolean nameActiveExists = tuple.getT3();

                    if (nameActiveExists)
                        return Mono.error(new BusinessRuleException(
                                "Já existe um produto ATIVO com este nome no sistema."
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

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> allProducts(Pageable pageable){

        return PageUtils.toPage(
                productRepository.findAllBy(pageable),
                productRepository.count(),
                productMapper::toProductResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os produtos"))
                .doOnNext(page ->
                        log.info("Quantidade de produtos encontrados com sucesso : {}", page.getTotalElements()));
    }

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> findAllProductsByStatus(ProductStatus status, Pageable pageable){

        return PageUtils.toPage(
                productRepository.findAllByStatus(status, pageable),
                productRepository.countByStatus(status),
                productMapper::toProductResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando todos os produtos de acordo com o status inserido"))
                .doOnNext(page ->
                        log.info("Quantidade de produtos encontrados: {} com o status: {}",
                                page.getTotalElements(), status));
    }

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> findProductByCategory(UUID categoryId, Pageable pageable){

        return categoryRepository.existsById(categoryId)
                .flatMap(exists -> {

                    if (!exists) return Mono.error(new CategoryNotFoundException());

                    return PageUtils.toPage(
                            productRepository.findAllByCategoryId(categoryId, pageable),
                            productRepository.countByCategoryId(categoryId),
                            productMapper::toProductResponse,
                            pageable
                    );
                })
                .doFirst(() -> log.info("Buscando produtos da categoria: {}", categoryId))
                .doOnNext(page ->
                        log.info("A quantidade de produtos encontrados foram de: {} da categoria: {}",
                                page.getTotalElements(), categoryId));
    }

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> findProductBySupplier(UUID supplierId, Pageable pageable){

        return suppliersRepository.existsById(supplierId)
                .flatMap(existSupplier -> {

                    if (!existSupplier) return Mono.error(new SupplierNotFoundException());

                    return PageUtils.toPage(
                            productRepository.findAllBySupplierId(supplierId, pageable),
                            productRepository.countBySupplierId(supplierId),
                            productMapper::toProductResponse,
                            pageable
                    );
                })
                .doFirst(() -> log.info("Buscando produtos do fornecedor: {}", supplierId))
                .doOnNext(page ->
                        log.info("Quantidade: {} de produtos encontrados do fornecedor: {}",
                                page.getTotalElements(), supplierId));
    }

    @Transactional(readOnly = true)
    public Mono<ProductResponse> getProductById(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Buscando produto pelo ID: {}", id))
                .doOnNext(response ->
                        log.info("Produto: {}, com o ID: {} encontrado com sucesso",
                                response.name(), response.id()));
    }

    @Transactional(readOnly = true)
    public Mono<ProductResponse> getProductSku(String sku){

        return productRepository.findBySku(sku)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Buscando produto pelo SKU: {}", sku))
                .doOnNext(response ->
                        log.info("Produto: {}, com SKU {} encontrado com sucesso",
                                response.name(), response.sku()));

    }

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> searchName(String name, Pageable pageable){

        return PageUtils.toPage(
                productRepository.searchByName(name, pageable),
                productRepository.countByName(name),
                productMapper::toProductResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando Produto pelo nome: {}", name))
                .doOnNext(page ->
                        log.info("Produtos com o nome: {} encontrados com sucesso", name));
    }

    @Transactional(readOnly = true)
    public Mono<Page<ProductResponse>> searchNameByStatus(String name, ProductStatus status, Pageable pageable){

        return PageUtils.toPage(
                productRepository.searchNameAndStatus(name, status, pageable),
                productRepository.countNameAndStatus(name, status),
                productMapper::toProductResponse,
                pageable
        )
                .doFirst(() -> log.info("Buscando produtos pelo nome: {} e status: {}", name, status))
                .doOnNext(page ->
                        log.info("Produtos encontrados: {}, com o nome: {} e status: {}",
                                page.getTotalElements(), name, status));
    }

    @Transactional
    public Mono<Void> activeProduct(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMap(product -> {

                    if (product.isActive()) return
                            Mono.error(new BusinessRuleException("O produto ja se encontra ativado"));

                    product.activateProduct();

                    return productRepository.save(product);
                })
                .doFirst(() -> log.info("Ativando produto: {}", id))
                .doOnSuccess(v -> log.info("Produto: '{}' ativado com sucesso", v.getName()))
                .then();
    }

    @Transactional
    public Mono<Void> discontinueProduct(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMap(product -> {

                    if (ProductStatus.DISCONTINUED.equals(product.getStatus())) return
                    Mono.error(new BusinessRuleException("O produto já se encontra descontinuado"));

                    product.discontinuedProduct();

                    return productRepository.save(product);
                })
                .doFirst(() -> log.info("Iniciando o processo para descontinuar o produto"))
                .doOnSuccess(v -> log.info("Produto: '{}' descontinuado com sucesso", v.getName()))
                .then();
    }

    @Transactional
    public Mono<Void> inactiveProduct(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMap(product -> {

                    if (ProductStatus.INACTIVE.equals(product.getStatus()))
                        return Mono.error(new BusinessRuleException("O produto já se encontra desativado"));

                    product.inactiveProduct();

                    return productRepository.save(product);
                })
                .doFirst(() -> log.warn("Iniciando processo para desativar produto"))
                .doOnSuccess(v -> log.info("Produto: '{}' desativado com sucesso", v.getName()))
                .then();
    }

    @Transactional
    public Mono<ProductResponse> updateProduct(UUID id, ProductUpdateRequest request){

        Mono<Product> productMono = productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()));

        Mono<Boolean> existsNameAndStatusMono = productRepository
                .existsByNameAndStatus(request.name(), ProductStatus.ACTIVE);

        return Mono.zip(productMono, existsNameAndStatusMono)
                .flatMap(tuple -> {

                    Product product = tuple.getT1();
                    Boolean existsNameAndStatus = tuple.getT2();

                    if (existsNameAndStatus && !product.getName().equalsIgnoreCase(request.name()))
                        return  Mono.error(new BusinessRuleException(
                                "Já existe um produto ativo com esse nome"));

                    productMapper.toUpdateProduct(product, request);

                    return productRepository.save(product);

                })
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Atualizando o produto: {}", id))
                .doOnNext(response ->
                        log.info("Produto: '{}' atualizado com sucesso", response.name()));
    }

    @Transactional
    public Mono<Void> deleteProduct(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .flatMap(product -> {

                    if (product.isActive()) return Mono.error(new BusinessRuleException(
                            "Desative ou descontinue o produto antes de deletar."
                    ));

                    return productRepository.delete(product);

                })
                .doFirst(() -> log.warn("Iniciando processo para deletar o produto: {}", id))
                .doOnSuccess(v -> log.info("Produto deletado com sucesso"))
                .then();
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
                            log.warn("SKU {} já existe (tentativa {}), gerando novo...", sku, attempt);
                            return generateUniqueSku(
                                    categoryName, supplierName, productName, attempt + 1);
                        }
                        return Mono.just(sku);
                    });
        });
    }
}
