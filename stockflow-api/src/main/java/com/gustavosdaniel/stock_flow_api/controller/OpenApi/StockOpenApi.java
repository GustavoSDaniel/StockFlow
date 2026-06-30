package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import java.util.UUID;

import com.gustavosdaniel.stock_flow_api.domain.dto.request.InventoryMovementRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.StockUpdate;
import com.gustavosdaniel.stock_flow_api.domain.dto.request.TransferRequest;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.InventoryMovementResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockResponse;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.StockSummaryResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import jakarta.validation.Valid;

@Tag(name = "Stocks", description = "Controle de estoque")
public interface StockOpenApi {

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

    @Operation(summary = "Buscar estoques por produto",
            description = "Retorna todos os estoques associados a um determinado produto")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Estoques do produto",
                    content = @Content(mediaType = "application/json",
                            array = @ArraySchema(schema = @Schema(implementation = StockResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Produto não encontrado", content = @Content)
    })
    Flux<ResponseEntity<StockResponse>> getStockByProduct(
            @Parameter(description = "ID do produto", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId);

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
            @PageableDefault(size = 20, sort = "productName", direction = Sort.Direction.DESC)
            Pageable pageable);

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
    Mono<ResponseEntity<Void>> transferStock(   // ← nome atualizado
                                                @Parameter(description = "ID do produto de origem", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
                                                @PathVariable UUID productId,
                                                @Valid @org.springframework.web.bind.annotation.RequestBody TransferRequest request);

    @Operation(summary = "Produtos sem estoque",
            description = "Retorna produtos cujo estoque está zerado (out of stock)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos sem estoque"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findOutOfStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)   // ← sem sort
            Pageable pageable);

    @Operation(summary = "Produtos com estoque baixo",
            description = "Retorna produtos cujo estoque está abaixo do nível mínimo definido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos com estoque baixo"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findLowStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)   // ← sem sort
            Pageable pageable);

    @Operation(summary = "Produtos com excesso de estoque",
            description = "Retorna produtos cujo estoque está acima do nível máximo definido")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de produtos com excesso de estoque"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<StockSummaryResponse>>> findOverStock(
            @ParameterObject
            @PageableDefault(size = 20, direction = Sort.Direction.ASC)   // ← sem sort
            Pageable pageable);

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
}