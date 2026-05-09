package com.gustavosdaniel.stock_flow_api.util;

import com.gustavosdaniel.stock_flow_api.domain.dto.response.ErrorDocResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class ErrorDoc {

    private final Map<String, ErrorDocResponse> docs = new HashMap<>();

    public ErrorDoc() {
        docs.put("validacao", new ErrorDocResponse(
                "Validação falhou",
                "Erro de validação nos campos da requisição.",
                "Algum campo está ausente, vazio ou em formato inválido.",
                "Verifique 'fieldsErrors' na resposta e corrija os campos.",
                400
        ));
        docs.put("usuario-nao-encontrado", new ErrorDocResponse(
                "Usuário não encontrado",
                "Não foi possível encontrar o usuário.",
                "O ID fornecido não existe.",
                "Verifique se o ID está correto.",
                404
        ));
        docs.put("nao-autorizado", new ErrorDocResponse(
                "Acesso negado",
                "Usuário sem permissão para realizar essa operação.",
                "Seu nível de acesso não permite essa ação.",
                "Solicite ao administrador a permissão necessária.",
                403
        ));
        docs.put("estoque-insuficiente", new ErrorDocResponse(
                "Estoque insuficiente",
                "Quantidade em estoque menor que a solicitada.",
                "A saída solicitada ultrapassa o estoque disponível.",
                "Verifique a quantidade disponível antes de realizar a operação.",
                400
        ));
        docs.put("quantidade-invalida", new ErrorDocResponse(
                "Quantidade inválida",
                "A quantidade informada não é válida.",
                "Quantidades devem ser maiores que zero.",
                "Informe um valor inteiro positivo.",
                400
        ));
        docs.put("erro-interno", new ErrorDocResponse(
                "Erro interno",
                "Ocorreu um erro inesperado no servidor.",
                "Erro não previsto no sistema.",
                "Tente novamente. Se persistir, contate o suporte.",
                500
        ));
    }

    public  Map<String, ErrorDocResponse> findAll(){
        return Collections.unmodifiableMap(docs);
    }

    public  Mono<ErrorDocResponse> find(String erroKey){

        return Mono.justOrEmpty(docs.get(erroKey));
    }
}
