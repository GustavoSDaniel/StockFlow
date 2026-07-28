package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import java.util.UUID;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockUpdate;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.TransferRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.StockStatus;

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
 * OpenAPI contract for the {@code /api/v1/stocks} endpoints.
 * Documents stock CRUD, inventory movements (entry/exit/adjust/transfer), status queries, and PDF report generation.
 */
@Tag(name = "Stocks", description = "Controle de estoque")
public interface StockOpenApi {

    /**
     * Creates a stock record for the specified product.
     *
     * @param productId the product ID
     * @param request   the initial stock data
     * @return the created stock record with HTTP 201
     */
    @Operation(
            summary = "Criar estoque para um produto",
            description = "Cria um registro de estoque para o produto especificado",
            requestBody = @RequestBody(
                    description = "Dados iniciais do estoque",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StockRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Estoque criado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<StockResponse>> createStock(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId,
            @Valid @org.springframework.web.bind.annotation.RequestBody StockRequest request);

    /**
     * Returns a specific stock record by its ID.
     *
     * @param id the stock record ID
     * @return the stock record, or HTTP 404 if not found
     */
    @Operation(summary = "Buscar estoque por ID",
            description = "Retorna um registro de estoque específico pelo seu ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<StockResponse>> getStockById(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id);

    /**
     * Returns all stock records associated with a specific product.
     *
     * @param productId the product ID
     * @param pageable  pagination parameters (default: size=20)
     * @return a page of stock records for the product
     */
    @Operation(summary = "Buscar estoques por produto",
            description = "Retorna uma página com todos os estoques associados a um determinado produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de estoques do produto"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockResponse>>> getStockByProduct(   // ← atualizado (Flux → Mono<Page>)
                                                                   @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
                                                                   @PathVariable UUID productId,
                                                                   @ParameterObject @PageableDefault(size = 20) Pageable pageable);  // ← adicionado Pageable

    /**
     * Returns a paginated list of all stock records.
     *
     * @param pageable pagination parameters (default: size=20)
     * @return a page of stock summaries
     */
    @Operation(summary = "Listar todos os estoques",
            description = "Retorna uma página com todos os registros de estoque, com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de estoques"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> allStocks(
            @ParameterObject
            @PageableDefault(size = 20)
            Pageable pageable);

    /**
     * Returns the paginated movement history (entries, exits, adjustments) of a stock record.
     *
     * @param stockId  the stock record ID
     * @param pageable pagination and sorting parameters (default: size=20, sort=createdAt DESC)
     * @return a page of inventory movements
     */
    @Operation(summary = "Histórico de movimentações de um estoque",
            description = "Retorna o histórico paginado de entradas, saídas e ajustes de um estoque")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de movimentações"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Page<InventoryMovementResponse>>> getMovementHistory(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID stockId,
            @ParameterObject
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) // ← "productName" → "createdAt"
            Pageable pageable);

    /**
     * Registers an inventory entry movement (stock increase).
     *
     * @param id      the stock record ID
     * @param request the entry movement data
     * @return HTTP 204 on success
     */
    @Operation(
            summary = "Registrar entrada no estoque",
            description = "Adiciona uma movimentação de entrada ao estoque especificado",
            requestBody = @RequestBody(
                    description = "Dados da movimentação de entrada",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InventoryMovementRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Entrada registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> registerEntry(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody InventoryMovementRequest request);

    /**
     * Registers an inventory exit movement (stock decrease).
     *
     * @param id      the stock record ID
     * @param request the exit movement data
     * @return HTTP 204 on success
     */
    @Operation(
            summary = "Registrar saída do estoque",
            description = "Adiciona uma movimentação de saída ao estoque especificado",
            requestBody = @RequestBody(
                    description = "Dados da movimentação de saída",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InventoryMovementRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Saída registrada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> registerExit(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody InventoryMovementRequest request);

    /**
     * Registers a manual stock adjustment (positive or negative quantity change).
     *
     * @param id      the stock record ID
     * @param request the adjustment movement data
     * @return HTTP 204 on success
     */
    @Operation(
            summary = "Ajustar estoque",
            description = "Realiza um ajuste manual na quantidade do estoque (positivo ou negativo)",
            requestBody = @RequestBody(
                    description = "Dados do ajuste",
                    required = true,
                    content = @Content(schema = @Schema(implementation = InventoryMovementRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Ajuste realizado com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> adjustStock(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody InventoryMovementRequest request);

    /**
     * Transfers a stock quantity from one product to another.
     *
     * @param productId the source product ID
     * @param request   the transfer details (target product, quantity, reason)
     * @return HTTP 204 on success
     */
    @Operation(
            summary = "Transferir estoque entre produtos",
            description = "Transfere uma quantidade de estoque de um produto para outro",
            requestBody = @RequestBody(
                    description = "Dados da transferência",
                    required = true,
                    content = @Content(schema = @Schema(implementation = TransferRequest.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Transferência realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto origem/destino não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> transferStock(
            @Parameter(description = "ID do produto de origem", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId,
            @Valid @org.springframework.web.bind.annotation.RequestBody TransferRequest request);

    /**
     * Returns products whose stock is zero (out of stock).
     *
     * @param pageable pagination parameters (default: size=20, sort ASC)
     * @return a page of out-of-stock products
     */
    @Operation(summary = "Produtos sem estoque",
            description = "Retorna produtos cujo estoque está zerado (out of stock)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos sem estoque"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findOutOfStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns products whose stock is below the defined minimum level.
     *
     * @param pageable pagination parameters (default: size=20, sort ASC)
     * @return a page of low-stock products
     */
    @Operation(summary = "Produtos com estoque baixo",
            description = "Retorna produtos cujo estoque está abaixo do nível mínimo definido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos com estoque baixo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findLowStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Returns products whose stock is above the defined maximum level.
     *
     * @param pageable pagination parameters (default: size=20, sort ASC)
     * @return a page of over-stock products
     */
    @Operation(summary = "Produtos com excesso de estoque",
            description = "Retorna produtos cujo estoque está acima do nível máximo definido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos com excesso de estoque"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findOverStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Updates a stock record's data (e.g., minimum/maximum quantity).
     *
     * @param id      the stock record ID
     * @param request the stock update payload
     * @return the updated stock record
     */
    @Operation(
            summary = "Atualizar estoque",
            description = "Atualiza os dados de um registro de estoque (ex.: quantidade mínima, máxima, etc.)",
            requestBody = @RequestBody(
                    description = "Dados atualizados do estoque",
                    required = true,
                    content = @Content(schema = @Schema(implementation = StockUpdate.class))
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoque atualizado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = StockResponse.class))),
            @ApiResponse(responseCode = "400", description = "Dados inválidos", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Estoque não encontrado", content = @Content)
    })
    Mono<ResponseEntity<StockResponse>> updateStock(
            @Parameter(description = "ID do estoque", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Valid @org.springframework.web.bind.annotation.RequestBody StockUpdate request);

    /**
     * Generates and downloads a PDF report of stock records, optionally filtered by product status.
     *
     * @param status optional product status filter for the report
     * @return the PDF report as a byte array
     */
    @Operation(summary = "Exportar relatório de estoque em PDF",
            description = "Gera e faz o download de um relatório PDF com os estoques cadastrados. "
                    + "Opcionalmente, é possível filtrar por status do produto.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Relatório PDF gerado com sucesso",
                    content = @Content(mediaType = "application/pdf",
                            schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<byte[]>> downloadStockReport(
            @Parameter(description = "Status opcional para filtrar os estoques do relatório", required = false, example = "ACTIVE")
            @RequestParam(required = false) StockStatus status);
}