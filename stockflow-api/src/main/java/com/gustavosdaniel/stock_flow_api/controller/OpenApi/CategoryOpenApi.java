package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.CategoryUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.CategoryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Tag(name = "Categories", description = "Gerenciamento de categorias (hierarquia, ativação, desativação, etc.)")
public interface CategoryOpenApi {

    @Operation(summary = "Criar nova categoria",
            description = "Cria uma categoria raiz ou subcategoria (depende do parentId informado no request)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Categoria criada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito (ex.: nome já existe)", content = @Content)
    })
    Mono<ResponseEntity<CategoryResponse>> createCategory(
            @Parameter(description = "Dados da nova categoria", required = true)
            @Valid @RequestBody CategoryRequest request);

    @Operation(summary = "Adicionar subcategoria",
            description = "Associa uma categoria existente como subcategoria de outra (relacionamento pai-filho)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subcategoria adicionada com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Requisição inválida (ex.: pai e filho iguais)",
                    content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria pai ou filho não encontrada", content = @Content)
    })
    Mono<ResponseEntity<CategoryResponse>> addSubcategory(
            @Parameter(description = "ID da categoria pai", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID parentId,
            @Parameter(description = "ID da categoria filha", required = true, example = "987fcdeb-51a2-43d7-9abc-123456789abc")
            @PathVariable UUID childId);

    @Operation(summary = "Listar todas as categorias",
            description = "Retorna uma página com todas as categorias (ativas e inativas), ordenadas por nome.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de categorias retornada",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> allCategories(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Pesquisar categorias por nome",
            description = "Busca categorias (sem filtrar por status) cujo nome contenha o termo informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> searchCategories(
            @Parameter(description = "Nome ou parte do nome para busca", required = true, example = "Eletrônicos")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Pesquisar categorias ativas por nome",
            description = "Busca apenas categorias com status ATIVO, cujo nome contenha o termo informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> searchActiveCategories(
            @Parameter(description = "Nome ou parte do nome para busca", required = true, example = "Livros")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Listar todas as subcategorias de uma categoria pai",
            description = "Retorna todas as subcategorias (ativas e inativas) de uma determinada categoria pai")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de subcategorias",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria pai não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> allSubcategoriesCategories(
            @Parameter(description = "ID da categoria pai", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Listar subcategorias ativas",
            description = "Retorna apenas as subcategorias com status ATIVO de uma categoria pai")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de subcategorias ativas",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria pai não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> allActiveSubcategories(
            @Parameter(description = "ID da categoria pai", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Listar subcategorias desativadas",
            description = "Retorna apenas as subcategorias com status INATIVO de uma categoria pai")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de subcategorias inativas",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria pai não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> allDisableSubcategories(
            @Parameter(description = "ID da categoria pai", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID parentId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Listar categorias desativadas",
            description = "Retorna todas as categorias (raízes ou não) com status INATIVO")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de categorias inativas",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<CategoryResponse>>> allDisableCategories(
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    @Operation(summary = "Atualizar categoria",
            description = "Altera os dados de uma categoria existente (nome, descrição, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Categoria atualizada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = CategoryResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    Mono<ResponseEntity<CategoryResponse>> updateCategory(
            @Parameter(description = "ID da categoria a ser atualizada", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Dados para atualização", required = true)
            @Valid @RequestBody CategoryUpdateRequest request);

    @Operation(summary = "Ativar categoria",
            description = "Altera o status da categoria para ATIVO (útil para reativar uma categoria inativa)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria ativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Void>> activeCategory(
            @Parameter(description = "ID da categoria a ser ativada", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    @Operation(summary = "Remover associação entre subcategoria e pai",
            description = "Desvincula uma subcategoria da sua categoria pai (a subcategoria passa a ser raiz)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Associação removida com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria pai ou filha não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Void>> removeCategory(
            @Parameter(description = "ID da categoria pai", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID parentId,
            @Parameter(description = "ID da categoria filha", required = true,
                    example = "987fcdeb-51a2-43d7-9abc-123456789abc")
            @PathVariable UUID childId);

    @Operation(summary = "Desativar categoria",
            description = "Altera o status da categoria para INATIVO. Categorias inativas não aparecem na maioria das consultas.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria desativada com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content)
    })
    Mono<ResponseEntity<Void>> disableCategory(
            @Parameter(description = "ID da categoria a ser desativada", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    @Operation(summary = "Excluir categoria permanentemente",
            description = "Remove a categoria do banco de dados. Cuidado: pode violar integridade referencial se houver produtos associados.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Categoria excluída com sucesso"),
            @ApiResponse(responseCode = "400", description = "ID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Categoria não encontrada", content = @Content),
            @ApiResponse(responseCode = "409", description = "Conflito (ex.: categoria possui produtos associados)", content = @Content)
    })
    Mono<ResponseEntity<Void>> deleteCategory(
            @Parameter(description = "ID da categoria a ser excluída", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);
}
