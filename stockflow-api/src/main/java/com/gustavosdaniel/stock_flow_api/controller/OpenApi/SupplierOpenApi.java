package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import java.util.UUID;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.AddressRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierContactRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.SupplierUpdateRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.*;
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
 * OpenAPI contract for the {@code /api/v1/suppliers} endpoints.
 * Documents supplier CRUD, address and contact management, and search operations.
 */
@Tag(name = "Suppliers", description = "Gerenciamento de fornecedores")
public interface SupplierOpenApi {

    /**
     * Creates a new supplier and returns the registered data.
     *
     * @param request the supplier creation payload
     * @return the created supplier with HTTP 201
     */
    @Operation(
            summary = "Criar fornecedor",
            description = "Cria um novo fornecedor e retorna os dados cadastrados",
            requestBody = @RequestBody(
                    description = "Dados do fornecedor a ser criado",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SupplierRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Fornecedor criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content)
    })
    Mono<ResponseEntity<SupplierResponse>> createSupplier(
            @Valid @org.springframework.web.bind.annotation.RequestBody SupplierRequest request);   // ← Spring

    /**
     * Returns a paginated list of all suppliers with sorting support.
     *
     * @param pageable pagination and sorting parameters (default: size=20, sort=tradeName ASC)
     * @return a page of supplier summaries
     */
    @Operation(summary = "Listar todos os fornecedores",
            description = "Retorna uma página de fornecedores com suporte a paginação e ordenação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de fornecedores retornada"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<SupplierSummaryResponse>>> allSupplier(
            @ParameterObject
            @PageableDefault(size = 20, sort = "tradeName", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns the supplier matching the given CNPJ.
     *
     * @param cnpj the CNPJ number (digits only, e.g., "12345678000199")
     * @return the supplier, or HTTP 404 if not found
     */
    @Operation(summary = "Buscar fornecedor por CNPJ",
            description = "Retorna o fornecedor correspondente ao CNPJ informado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fornecedor encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<SupplierResponse>> findCnpj(
            @Parameter(description = "CNPJ (apenas números)", required = true, example = "12345678000199")
            @RequestParam String cnpj);

    /**
     * Searches suppliers by name (partial match) with pagination.
     *
     * @param name     the search term (partial name match)
     * @param pageable pagination and sorting parameters (default: size=20, sort=name ASC)
     * @return a page of matching suppliers
     */
    @Operation(summary = "Pesquisar fornecedores por nome",
            description = "Busca fornecedores cujo nome contenha o termo informado, com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados da pesquisa"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<SupplierSummaryResponse>>> searchName(
            @Parameter(description = "Parte do nome do fornecedor", required = true, example = "Distribuidora")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Searches suppliers by trade name (partial match) with pagination.
     *
     * @param tradeName the search term (partial trade name match)
     * @param pageable  pagination and sorting parameters (default: size=20, sort=tradeName ASC)
     * @return a page of matching suppliers
     */
    @Operation(summary = "Pesquisar fornecedores por nome fantasia",
            description = "Busca fornecedores cujo nome fantasia contenha o termo informado, com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados paginados da pesquisa"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<SupplierSummaryResponse>>> searchTradeName(
            @Parameter(description = "Parte do nome fantasia", required = true, example = "Global")
            @RequestParam String tradeName,
            @ParameterObject
            @PageableDefault(size = 20, sort = "tradeName", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Adds a new address to the specified supplier.
     *
     * @param supplierId the supplier ID
     * @param request    the address data
     * @return the created address with HTTP 201
     */
    @Operation(
            summary = "Adicionar endereço ao fornecedor",
            description = "Vincula um novo endereço ao fornecedor especificado",
            requestBody = @RequestBody(
                    description = "Dados do endereço",
                    required = true,
                    content = @Content(schema = @Schema(implementation = AddressRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Endereço adicionado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddressResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<AddressResponse>> addAddress(
            @Parameter(description = "ID do fornecedor", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID supplierId,
            @Valid @org.springframework.web.bind.annotation.RequestBody AddressRequest request);   // ← Spring

    /**
     * Deletes a supplier's address.
     *
     * @param addressId the address ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Remover endereço",
            description = "Exclui um endereço do fornecedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Endereço removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Endereço não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> deleteAddress(
            @Parameter(description = "ID do endereço", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID addressId);

    /**
     * Adds a new contact to the specified supplier.
     *
     * @param supplierId the supplier ID
     * @param request    the contact data
     * @return the created contact with HTTP 201
     */
    @Operation(
            summary = "Adicionar contato ao fornecedor",
            description = "Vincula um novo contato ao fornecedor especificado",
            requestBody = @RequestBody(
                    description = "Dados do contato",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SupplierContactRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Contato adicionado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierContactResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<SupplierContactResponse>> addContact(
            @Parameter(description = "ID do fornecedor", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID supplierId,
            @Valid @org.springframework.web.bind.annotation.RequestBody SupplierContactRequest request);   // ← Spring

    /**
     * Deletes a supplier's contact.
     *
     * @param contactId the contact ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Remover contato",
            description = "Exclui um contato do fornecedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contato removido com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Contato não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> removeContact(
            @Parameter(description = "ID do contato", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID contactId);

    /**
     * Updates an existing supplier's data.
     *
     * @param supplierId the supplier ID
     * @param request    the update payload
     * @return the updated supplier
     */
    @Operation(
            summary = "Atualizar fornecedor",
            description = "Atualiza os dados de um fornecedor existente",
            requestBody = @RequestBody(
                    description = "Dados atualizados do fornecedor",
                    required = true,
                    content = @Content(schema = @Schema(implementation = SupplierUpdateRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fornecedor atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = SupplierUpdateResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<SupplierUpdateResponse>> updateSupplier(
            @Parameter(description = "ID do fornecedor", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID supplierId,
            @Valid @org.springframework.web.bind.annotation.RequestBody SupplierUpdateRequest request);   // ← Spring

    /**
     * Permanently deletes a supplier.
     *
     * @param supplierId the supplier ID
     * @return HTTP 204 on success
     */
    @Operation(summary = "Excluir fornecedor",
            description = "Remove permanentemente um fornecedor")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Fornecedor excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Fornecedor não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> deleteSupplier(
            @Parameter(description = "ID do fornecedor", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID supplierId);
}


