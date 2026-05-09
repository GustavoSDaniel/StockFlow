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

@Tag(name = "Errors", description = "Consulta da documentação de erros da aplicação")
public interface ErrorOpenApi {

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
