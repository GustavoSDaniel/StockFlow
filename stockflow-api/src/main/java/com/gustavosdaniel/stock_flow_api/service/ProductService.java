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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Service for product lifecycle management including creation, search, status transitions,
 * update, and deletion, with SKU generation and price validation.
 */
@Service
public class ProductService {

    private static final Logger log = LoggerFactory.getLogger(ProductService.class);
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

    /**
     * Creates a new product after validating category, supplier, name uniqueness, and price consistency.
     * A unique SKU is auto-generated.
     *
     * @param request the product creation payload
     * @return a Mono emitting the created product response
     * @throws CategoryNotFoundException if the referenced category does not exist
     * @throws SupplierNotFoundException if the referenced supplier does not exist
     * @throws BusinessRuleException if an active product with the same name already exists,
     *                               or if the sale price is lower than the cost price
     */
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

                    validateSaleAndCost(request.salePrice(), request.costPrice());

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

    /**
     * Retrieves a paginated list of all products.
     *
     * @param pageable pagination information
     * @return a Mono emitting a page of product responses
     */
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

    /**
     * Streams all products as a Flux for report generation (no pagination).
     *
     * @return a Flux emitting all product responses
     */
    @Transactional(readOnly = true)
    public Flux<ProductResponse> allProductsForReport(){

        return productRepository.findAll()
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Buscando todos os produtos para geração de relatório PDF"))
                .doOnComplete(() -> log.info("Busca de produtos para relatório finalizada com sucesso."));
    }

    /**
     * Retrieves a paginated list of products filtered by a given status.
     *
     * @param status   the product status to filter by
     * @param pageable pagination information
     * @return a Mono emitting a page of matching product responses
     */
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

    /**
     * Retrieves a paginated list of products belonging to a specific category.
     *
     * @param categoryId the category ID
     * @param pageable   pagination information
     * @return a Mono emitting a page of matching product responses
     * @throws CategoryNotFoundException if the category does not exist
     */
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

    /**
     * Retrieves a paginated list of products supplied by a specific supplier.
     *
     * @param supplierId the supplier ID
     * @param pageable   pagination information
     * @return a Mono emitting a page of matching product responses
     * @throws SupplierNotFoundException if the supplier does not exist
     */
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
                        log.info("Quantidade de produtos encontrados: {}  do fornecedor: {}",
                                page.getTotalElements(), supplierId));
    }

    /**
     * Retrieves a single product by its ID.
     *
     * @param id the product ID
     * @return a Mono emitting the product response
     * @throws ProductNotFoundException if the product does not exist
     */
    @Transactional(readOnly = true)
    public Mono<ProductResponse> getProductById(UUID id){

        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new ProductNotFoundException()))
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Buscando produto pelo ID: {}", id))
                .doOnNext(response ->
                        log.info("Produto: {}, com o SKU: {} encontrado com sucesso",
                                response.name(), response.sku()));
    }

    /**
     * Retrieves a single product by its SKU.
     *
     * @param sku the product SKU
     * @return a Mono emitting the product response
     * @throws ProductNotFoundException if no product matches the SKU
     */
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

    /**
     * Searches products by name (case-insensitive partial match).
     *
     * @param name     the search term
     * @param pageable pagination information
     * @return a Mono emitting a page of matching product responses
     */
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

    /**
     * Searches products by name and status simultaneously.
     *
     * @param name     the search term
     * @param status   the product status to filter by
     * @param pageable pagination information
     * @return a Mono emitting a page of matching product responses
     */
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

    /**
     * Activates an inactive or discontinued product.
     *
     * @param id the product ID
     * @return a Mono that completes when the operation is done
     * @throws ProductNotFoundException if the product does not exist
     * @throws BusinessRuleException if the product is already active
     */
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

    /**
     * Marks a product as discontinued.
     *
     * @param id the product ID
     * @return a Mono that completes when the operation is done
     * @throws ProductNotFoundException if the product does not exist
     * @throws BusinessRuleException if the product is already discontinued
     */
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

    /**
     * Marks a product as inactive.
     *
     * @param id the product ID
     * @return a Mono that completes when the operation is done
     * @throws ProductNotFoundException if the product does not exist
     * @throws BusinessRuleException if the product is already inactive
     */
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

    /**
     * Updates an existing product's fields. Validates name uniqueness and price consistency.
     *
     * @param id      the product ID
     * @param request the update payload
     * @return a Mono emitting the updated product response
     * @throws ProductNotFoundException if the product does not exist
     * @throws BusinessRuleException if an active product with the new name already exists,
     *                               or if the sale price is lower than the cost price
     */
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

                    validateSaleAndCost(product.getSalePrice(), product.getCostPrice());

                    return productRepository.save(product);

                })
                .map(productMapper::toProductResponse)
                .doFirst(() -> log.info("Atualizando o produto: {}", id))
                .doOnNext(response ->
                        log.info("Produto: '{}' atualizado com sucesso", response.name()));
    }

    /**
     * Permanently deletes a product. Only inactive or discontinued products can be deleted.
     *
     * @param id the product ID
     * @return a Mono that completes when the operation is done
     * @throws ProductNotFoundException if the product does not exist
     * @throws BusinessRuleException if the product is still active
     */
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

    private void validateSaleAndCost(BigDecimal salePrice, BigDecimal costPrice){

        if (salePrice.compareTo(costPrice) < 0)
            throw new BusinessRuleException("O preço de venda deve ser maior ou igual ao preço de custo.");
    }


}
