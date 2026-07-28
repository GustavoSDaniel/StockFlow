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

/**
 * OpenAPI contract for the {@code /api/v1/categories} endpoints.
 * Documents category CRUD, subcategory management, and activation/deactivation operations.
 */
@Tag(name = "Categories", description = "Gerenciamento de categorias (hierarquia, ativação, desativação, etc.)")
public interface CategoryOpenApi {

    /**
     * Creates a new category (root or subcategory depending on {@code parentId} in the request).
     *
     * @param request the category creation payload
     * @return the created category with HTTP 201
     */
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

    /**
     * Associates an existing category as a subcategory of another (parent-child relationship).
     *
     * @param parentId the ID of the parent category
     * @param childId  the ID of the child category to be linked
     * @return the updated parent category with HTTP 200
     */
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

    /**
     * Returns a paginated list of all categories (active and inactive), sorted by name.
     *
     * @param pageable pagination and sorting parameters (default: size=20, sort=name ASC)
     * @return a page of categories
     */
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

    /**
     * Searches categories by name (partial match), without filtering by status.
     *
     * @param name     the search term (partial name match)
     * @param pageable pagination and sorting parameters
     * @return a page of matching categories
     */
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

    /**
     * Searches only active categories by name (partial match).
     *
     * @param name     the search term (partial name match)
     * @param pageable pagination and sorting parameters
     * @return a page of matching active categories
     */
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

    /**
     * Returns all subcategories (active and inactive) of a given parent category.
     *
     * @param parentId the ID of the parent category
     * @param pageable pagination and sorting parameters
     * @return a page of subcategories
     */
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

    /**
     * Returns only active subcategories of a given parent category.
     *
     * @param parentId the ID of the parent category
     * @param pageable pagination and sorting parameters
     * @return a page of active subcategories
     */
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

    /**
     * Returns only disabled subcategories of a given parent category.
     *
     * @param parentId the ID of the parent category
     * @param pageable pagination and sorting parameters
     * @return a page of disabled subcategories
     */
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

    /**
     * Returns all disabled categories (root or not), regardless of parent.
     *
     * @param pageable pagination and sorting parameters (default: size=20, sort=name ASC)
     * @return a page of disabled categories
     */
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

    /**
     * Updates an existing category's data (name, description, etc.).
     *
     * @param id      the ID of the category to update
     * @param request the update payload
     * @return the updated category
     */
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

    /**
     * Activates a category, setting its status to ACTIVE.
     *
     * @param id the ID of the category to activate
     * @return HTTP 204 on success
     */
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

    /**
     * Removes the parent-child association between two categories, making the child a root category.
     *
     * @param parentId the ID of the parent category
     * @param childId  the ID of the child category to unlink
     * @return HTTP 204 on success
     */
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

    /**
     * Disables a category, setting its status to INACTIVE. Inactive categories are excluded from most queries.
     *
     * @param id the ID of the category to disable
     * @return HTTP 204 on success
     */
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

    /**
     * Permanently deletes a category. May fail with HTTP 409 if associated products exist.
     *
     * @param id the ID of the category to delete
     * @return HTTP 204 on success
     */
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
