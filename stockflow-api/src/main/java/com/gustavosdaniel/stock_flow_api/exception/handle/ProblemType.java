package com.gustavosdaniel.stock_flow_api.exception.handle;

import java.net.URI;

public enum ProblemType {

    VALIDATE_ERROR(

            "urn:stockflow:erro-de-validacao",
            "Validação falhou"
    ),

    UNAUTHORIZED(

            "urn:stockflow:nao-autorizado",
            "Não autorizado"
    ),

    USER_NOT_FOUND(

            "urn:stockflow:usuario-nao-encontrado",
            "Usuário não encontrado"
    ),

    ACCESS_DENIED(
            "urn:stockflow:acesso-negado",
            "Acesso negado"),

    INSUFFICIENT_STOCK(
            "urn:stockflow:estoque-insuficiente",
            "Estoque insuficiente"
    ),

    INVALID_QUANTITY(

            "urn:stockflow:quantidade-invalida",
            "Quantidade invalida"
    ),

    INTERNAL_ERROR(
            "urn:stockflow:erro-interno",
            "Erro interno"
    );

    private final URI uri;

    private final String title;

     ProblemType(String uri, String title) {
        this.uri = URI.create(uri);
        this.title = title;
    }

    public URI getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }
}
