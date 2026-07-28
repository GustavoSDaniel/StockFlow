package com.gustavosdaniel.stock_flow_api.controller.OpenApi;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.UserResponse;
import com.gustavosdaniel.stock_flow_api.domain.enums.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * OpenAPI contract for the {@code /api/v1/users} endpoints.
 * Documents user profile retrieval, listing, search, role promotion, and account status management.
 */
@Tag(name = "Users", description = "Gerenciamento de usuários")
public interface UserOpenApi {

    /**
     * Returns the currently authenticated user's data based on the JWT token.
     *
     * @param jwt the JWT authentication principal (injected by Spring Security)
     * @return the current user's profile
     */
    @Operation(summary = "Obter usuário autenticado",
            description = "Retorna os dados do usuário atual baseado no token JWT")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Usuário encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class))),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    Mono<ResponseEntity<UserResponse>> getUSer(
            @Parameter(hidden = true) @AuthenticationPrincipal Jwt jwt);

    /**
     * Returns a paginated list of all users with sorting support.
     *
     * @param pageable pagination and sorting parameters (default: size=20, sort=userName ASC)
     * @return a page of users
     */
    @Operation(summary = "Listar todos os usuários",
            description = "Retorna uma página de usuários com suporte a paginação e ordenação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Página de usuários retornada",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<UserResponse>>> getAllUsers(
            @ParameterObject
            @PageableDefault(size = 20, sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Searches users by name (partial match) with pagination.
     *
     * @param name     the search term (partial name match)
     * @param pageable pagination and sorting parameters (default: sort=userName ASC)
     * @return a page of matching users
     */
    @Operation(summary = "Pesquisar usuários por nome",
            description = "Busca usuários cujo nome contenha o termo informado, com paginação")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Resultados da pesquisa paginados",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content)
    })
    Mono<ResponseEntity<Page<UserResponse>>> searchUsers(
            @Parameter(description = "Nome ou parte do nome para busca", required = true, example = "João")
            @RequestParam String name,
            @ParameterObject
            @PageableDefault(sort = "userName", direction = Sort.Direction.ASC)
            Pageable pageable);

    /**
     * Changes the role of a specific user.
     *
     * @param targetUserId the ID of the target user
     * @param newRole      the new role to assign (e.g., ADMIN)
     * @return HTTP 204 on success
     */
    @Operation(summary = "Promover usuário",
            description = "Altera a role de um usuário específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Promoção realizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Requisição inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> promoteUser(
            @Parameter(description = "ID do usuário alvo", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID targetUserId,
            @Parameter(description = "Nova role a ser atribuída", required = true, example = "ADMIN")
            @RequestParam UserRole newRole);

    /**
     * Disables a user, revoking their access.
     *
     * @param targetUserId the ID of the user to disable
     * @return HTTP 204 on success
     */
    @Operation(summary = "Desabilitar usuário",
            description = "Desabilita um usuário, impedindo seu acesso")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário desabilitado com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> disableUser(
            @Parameter(description = "ID do usuário a ser desabilitado", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID targetUserId);

    /**
     * Activates a previously inactive user, restoring their access.
     *
     * @param targetUserId the ID of the user to activate
     * @return HTTP 204 on success
     */
    @Operation(summary = "Ativar usuário",
            description = "Ativa um usuário que estava inativo, permitindo seu acesso ao sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário ativado com sucesso (ou já estava ativo)"),
            @ApiResponse(responseCode = "400", description = "UUID inválido", content = @Content),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> activeUser(
            @Parameter(description = "ID do usuário a ser ativado", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID targetUserId);

    /**
     * Permanently deletes a user.
     *
     * @param targetUserId the ID of the user to delete
     * @return HTTP 204 on success
     */
    @Operation(summary = "Excluir usuário",
            description = "Remove permanentemente um usuário")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Usuário excluído com sucesso"),
            @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão", content = @Content),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado", content = @Content)
    })
    Mono<ResponseEntity<Void>> deleteUser(
            @Parameter(description = "ID do usuário a ser excluído", required = true,
                    example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID targetUserId);
}