package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.ProductOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import com.gustavosdaniel.stock_flow_api.service.ProductService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController implements ProductOpenApi {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public Mono<ResponseEntity<ProductResponse>> createProduct(@RequestBody @Valid ProductRequest request){

        return productService.createProduct(request)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @GetMapping
    public Mono<ResponseEntity<Page<ProductResponse>>> allProducts(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return productService.allProducts(pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/status")
    public Mono<ResponseEntity<Page<ProductResponse>>> findProductsByStatus(
            @RequestParam ProductStatus status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return productService.findAllProductsByStatus(status, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/category/{categoryId}")
    public Mono<ResponseEntity<Page<ProductResponse>>> findAllProductByCategory(
            @PathVariable UUID categoryId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return productService.findProductByCategory(categoryId, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/supplier/{supplierId}")
    public Mono<ResponseEntity<Page<ProductResponse>>> findAllProductBySupplier(
            @PathVariable UUID supplierId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable){

        return productService.findProductBySupplier(supplierId, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ProductResponse>> getProductId(@PathVariable UUID id){

        return productService.getProductById(id).map(ResponseEntity::ok);
    }

    @GetMapping("/sku")
    public Mono<ResponseEntity<ProductResponse>> getProductSku(@RequestParam String sku){

        return productService.getProductSku(sku).map(ResponseEntity::ok);
    }

    @GetMapping("/search-name")
    public Mono<ResponseEntity<Page<ProductResponse>>> searchProductName(
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return productService.searchName(name, pageable).map(ResponseEntity::ok);
    }

    @GetMapping("/search-name-status")
    public Mono<ResponseEntity<Page<ProductResponse>>> searchProductNameAndStatus(
            @RequestParam String name,
            @RequestParam ProductStatus status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable)
    {
        return productService.searchNameByStatus(name, status, pageable).map(ResponseEntity::ok);
    }

    @PatchMapping("/{id}/activate")
    public Mono<ResponseEntity<Void>> activateProduct(@PathVariable UUID id){

        return productService.activeProduct(id).thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/discontinue")
    public Mono<ResponseEntity<Void>> discontinueProduct(@PathVariable UUID id){

        return productService.discontinueProduct(id).thenReturn(ResponseEntity.noContent().build());
    }

    @PatchMapping("/{id}/inactive")
    public Mono<ResponseEntity<Void>> inactiveProduct(@PathVariable UUID id){

        return productService.inactiveProduct(id).thenReturn(ResponseEntity.noContent().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ProductResponse>> updateProduct(
            @PathVariable UUID id,
            @RequestBody @Valid ProductUpdateRequest request)
    {
        return productService.updateProduct(id, request).map(ResponseEntity::ok);
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteProduct(@PathVariable UUID id){

        return productService.deleteProduct(id).thenReturn(ResponseEntity.noContent().build());
    }
}
