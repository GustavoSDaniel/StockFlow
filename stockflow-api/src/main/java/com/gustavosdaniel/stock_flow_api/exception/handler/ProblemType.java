package com.gustavosdaniel.stock_flow_api.exception.handler;

import java.net.URI;

/**
 * Enumeration of RFC 7807 problem types used across the API.
 * <p>
 * Each constant carries a unique {@code URN} URI that identifies the error category
 * and a human-readable title that appears in {@link ProblemDetail} responses.
 * These values are consumed by {@link GlobalExceptionHandler} when building
 * standardized error payloads.
 * </p>
 */
public enum ProblemType {

    /** A domain business rule was violated. */
    BUSINESS_RULE(

            "urn:stockflows:regra-de-negocio",
            "Violação de regra de negócio"
    ),

    /** Request body validation failed. */
    VALIDATE_ERROR(

            "urn:stockflow:erro-de-validacao",
            "Validação falhou"
    ),

    /** The user lacks the required permissions. */
    UNAUTHORIZED(

            "urn:stockflow:nao-autorizado",
            "Não autorizado"
    ),

    /** An entity with the given name already exists. */
    NAME_EXIST(
            "urn:stockflow:nome-existe",
            "Nome já existente"
    ),

    /** The requested user was not found. */
    USER_NOT_FOUND(

            "urn:stockflow:usuario-nao-encontrado",
            "Usuário não encontrado"
    ),

    /** Access is denied due to insufficient role. */
    ACCESS_DENIED(
            "urn:stockflow:acesso-negado",
            "Acesso negado"),

    /** Stock level is insufficient for the requested movement. */
    INSUFFICIENT_STOCK(
            "urn:stockflow:estoque-insuficiente",
            "Estoque insuficiente"
    ),

    /** The supplied quantity is invalid (negative, zero, or out of range). */
    INVALID_QUANTITY(

            "urn:stockflow:quantidade-invalida",
            "Quantidade invalida"
    ),

    /** The requested category was not found. */
    CATEGORY_NOT_FOUND(
            "urn:stockflow:category-not-found",
            "Categoria não encontrada"
    ),

    /** An unexpected internal server error occurred. */
    INTERNAL_ERROR(
            "urn:stockflow:erro-interno",
            "Erro interno"
    ),
    /** The requested supplier was not found. */
    SUPPLIER_NOT_FOUND(

            "urn:stockflow:supplier-not-found",
            "Fornecedor não encontrada"
    ),
    /** An external service (e.g., ViaCEP) is temporarily unavailable. */
    EXTERNAL_SERVICE(
            "urn:stockflow:external-service",
            "Serviço temporariamente indisponível"
    ),
    /** The requested product was not found. */
    PRODUCT_NOT_FOUND(

            "urn:stockflow:produto-nao-encontrado",
            "Produto não encontrado"
    ),
    /** The requested stock record was not found. */
    STOCK_NOT_FOUND(

            "urn:stockflow:stock-nao-encontrado",
                    "Stock não encontrado"
    ),
    /** The requested notification was not found. */
    NOTIFICATION_NOT_FOUND(

            "urn:stockflow:notificacao-nao-encontrada",
            "Notificação não encontrada"
    ),
    /** An optimistic-locking concurrency conflict was detected. */
    CONCURRENCY_CONFLICT(
            "urn:stockflow:conflito-concorrencia",
            "Conflito de concorrência"
    );

    private final URI uri;

    private final String title;

    /**
     * Constructs a problem type with the given URN and human-readable title.
     *
     * @param uri   the URN string identifying the error category
     * @param title the human-readable title
     */
     ProblemType(String uri, String title) {
        this.uri = URI.create(uri);
        this.title = title;
    }

    /**
     * @return the URI that identifies this problem type
     */
    public URI getUri() {
        return uri;
    }

    /**
     * @return the human-readable title for this problem type
     */
    public String getTitle() {
        return title;
    }
}
