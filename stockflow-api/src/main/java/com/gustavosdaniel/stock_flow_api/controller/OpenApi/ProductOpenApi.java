package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import java.util.UUID;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.ProductUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ProductResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.ProductStatus;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

/**
 * OpenAPI contract for the {@code /api/v1/products} endpoints.
 * Documents product CRUD, status transitions, search, and PDF report generation.
 */
@Tag(name = "Products", description = "Gerenciamento de produtos")
public interface ProductOpenApi {

    /**
     * Creates a new product and returns its full data.
     *
     * @param request the product creation payload
     * @return the created product with HTTP 201
     */
    @Operation(
            summary = "Criar produto",
            description = "Cadastra um novo produto e retorna os dados completos",
            requestBody = @RequestBody(
                    description = "Dados do produto",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Produto criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    Mono<ResponseEntity<ProductResponse>> createProduct(
            @Valid @org.springframework.web.bind.annotation.RequestBody ProductRequest request);

    /**
     * Returns a paginated list of all products with sorting support.
     *
     * @param pageable pagination and sorting parameters (default: size=20, sort=name ASC)
     * @return a page of products
     */
    @Operation(summary = "Listar todos os produtos",
            description = "Retorna uma página de produtos com paginação e ordenação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> allProducts(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns products filtered by the given status.
     *
     * @param status   the product status to filter by (e.g., ACTIVE)
     * @param pageable pagination and sorting parameters
     * @return a page of products with the specified status
     */
    @Operation(summary = "Filtrar produtos por status",
            description = "Retorna uma página de produtos filtrados pelo status informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos filtrados por status"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> findProductsByStatus(
            @Parameter(description = "Status do produto", required = true, example = "ACTIVE")
            @RequestParam ProductStatus status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns products linked to a specific category.
     *
     * @param categoryId the ID of the category
     * @param pageable   pagination and sorting parameters
     * @return a page of products in the specified category
     */
    @Operation(summary = "Listar produtos por categoria",
            description = "Retorna produtos vinculados a uma categoria específica")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos da categoria"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> findAllProductByCategory(
            @Parameter(description = "ID da categoria", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID categoryId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns products linked to a specific supplier.
     *
     * @param supplierId the ID of the supplier
     * @param pageable   pagination and sorting parameters
     * @return a page of products from the specified supplier
     */
    @Operation(summary = "Listar produtos por fornecedor",
            description = "Retorna produtos vinculados a um fornecedor específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produtos do fornecedor"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> findAllProductBySupplier(
            @Parameter(description = "ID do fornecedor", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID supplierId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns a single product by its unique identifier.
     *
     * @param id the product ID
     * @return the product, or HTTP 404 if not found
     */
    @Operation(summary = "Buscar produto por ID",
            description = "Retorna um produto específico pelo seu identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<ProductResponse>> getProductId(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Returns the product that matches the given SKU code.
     *
     * @param sku the SKU code (e.g., "SKU-12345")
     * @return the product, or HTTP 404 if not found
     */
    @Operation(summary = "Buscar produto por SKU",
            description = "Retorna o produto correspondente ao código SKU informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<ProductResponse>> getProductSku(
            @Parameter(description = "SKU do produto", required = true, example = "SKU-12345")
            @RequestParam String sku);

    /**
     * Searches products by name (partial match) with pagination.
     *
     * @param name     the search term (partial name match)
     * @param pageable pagination and sorting parameters (default: size=20, sort=name ASC)
     * @return a page of matching products
     */
    @Operation(summary = "Pesquisar produtos por nome",
            description = "Busca produtos cujo nome contenha o termo informado, com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> searchProductName(
            @Parameter(description = "Parte do nome do produto", required = true, example = "Notebook")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Searches products by name and status combined.
     *
     * @param name     the search term (partial name match)
     * @param status   the product status filter
     * @param pageable pagination and sorting parameters
     * @return a page of matching products
     */
    @Operation(summary = "Pesquisar produtos por nome e status",
            description = "Busca produtos que atendam ao nome e ao status especificados")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<ProductResponse>>> searchProductNameAndStatus(
            @Parameter(description = "Parte do nome", required = true, example = "Monitor")
            @RequestParam String name,
            @Parameter(description = "Status do produto", required = true, example = "ACTIVE")
            @RequestParam ProductStatus status,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Activates a product, setting its status to ACTIVE.
     *
     * @param id the product ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Ativar produto",
            description = "Altera o status do produto para ATIVO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto ativado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> activateProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Marks a product as DISCONTINUED.
     *
     * @param id the product ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Descontinuar produto",
            description = "Marca o produto como DESCONTINUADO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto descontinuado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> discontinueProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Marks a product as INACTIVE.
     *
     * @param id the product ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Inativar produto",
            description = "Marca o produto como INATIVO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto inativado"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> inactiveProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Updates an existing product's data.
     *
     * @param id      the product ID
     * @param request the update payload
     * @return the updated product
     */
    @Operation(
            summary = "Atualizar produto",
            description = "Altera os dados de um produto existente",
            requestBody = @RequestBody(
                    description = "Dados atualizados do produto",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductUpdateRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Produto atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<ProductResponse>> updateProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody ProductUpdateRequest request);

    /**
     * Permanently deletes a product.
     *
     * @param id the product ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Excluir produto",
            description = "Remove permanentemente um produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Produto excluído"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> deleteProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Generates and downloads a PDF report of products, optionally filtered by status.
     *
     * @param status optional product status filter for the report
     * @return the PDF report as a byte array
     */
    @Operation(summary = "Exportar relatório de produtos em PDF",
            description = "Gera e faz o download de um relatório PDF com os produtos cadastrados. "
                    + "Opcionalmente, é possível filtrar por status.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório PDF gerado com sucesso",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<byte[]>> downloadProductReport(
            @Parameter(description = "Status opcional para filtrar os produtos do relatório", required = false, example = "ACTIVE")
            @RequestParam(required = false) ProductStatus status);
}