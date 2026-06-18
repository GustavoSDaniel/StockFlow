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

@Tag(name = "Products", description = "Gerenciamento de produtos")
public interface ProductOpenApi {

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
}
