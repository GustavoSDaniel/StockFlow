package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.ErrorDocResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * OpenAPI contract for the {@code /api/v1/errors} endpoints.
 * Provides error code lookups to document application-level error responses.
 */
@Tag(name = "Errors", description = "Consulta da documentação de erros da aplicação")
public interface ErrorOpenApi {

    /**
     * Returns a map of all documented errors, keyed by error code.
     *
     * @return a map of error keys to their documentation
     */
    @Operation(summary = "Listar todos os erros documentados",
            description = "Retorna um mapa onde a chave é o código do erro e o valor é o objeto de documentação correspondente.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentação de erros obtida com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(type = "object",
                                    additionalPropertiesSchema = ErrorDocResponse.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    Mono<ResponseEntity<Map<String, ErrorDocResponse>>> getAllErrorDoc();

    /**
     * Returns the documentation for a specific error identified by its key.
     *
     * @param errorKey the unique error identifier (e.g., "VALIDATION_ERROR")
     * @return the error documentation, or HTTP 404 if not found
     */
    @Operation(summary = "Buscar erro por chave",
            description = "Retorna a documentação de um erro específico identificado pela chave (errorKey).")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Documentação do erro encontrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = ErrorDocResponse.class))),
            @ApiResponse(responseCode = "404", description = "Nenhum erro encontrado com a chave informada", content = @Content),
            @ApiResponse(responseCode = "500", description = "Erro interno do servidor", content = @Content)
    })
    Mono<ResponseEntity<ErrorDocResponse>> getErrorDoc(
            @Parameter(description = "Chave única que identifica o erro", required = true, example = "VALIDATION_ERROR")
            @PathVariable String errorKey);
}
