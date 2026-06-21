package com.gustavosdaniel.stock_flow_api.exception.handle;

import java.net.URI;

public enum ProblemType {

    BUSINESS_RULE(

            "urn:stockflows:regra-de-negocio",
            "Violação de regra de negócio"
    ),

    VALIDATE_ERROR(

            "urn:stockflow:erro-de-validacao",
            "Validação falhou"
    ),

    UNAUTHORIZED(

            "urn:stockflow:nao-autorizado",
            "Não autorizado"
    ),

    NAME_EXIST(
            "urn:stockflow:nome-existe",
            "Nome já existente"
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

    CATEGORY_NOT_FOUND(
            "urn:stockflow:category-not-found",
            "Categoria não encontrada"
    ),

    INTERNAL_ERROR(
            "urn:stockflow:erro-interno",
            "Erro interno"
    ),
    SUPPLIER_NOT_FOUND(

            "urn:stockflow:supplier-not-found",
            "Fornecedor não encontrada"
    ),
    EXTERNAL_SERVICE(
            "urn:stockflow:external-service",
            "Serviço temporariamente indisponível"
    ),
    PRODUCT_NOT_FOUND(

            "urn:stockflow:produto-nao-encontrado",
            "Produto não encontrado"
    ),
    STOCK_NOT_FOUND(

            "urn:stockflow:stock-nao-encontrado",
                    "Stock não encontrado"
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
