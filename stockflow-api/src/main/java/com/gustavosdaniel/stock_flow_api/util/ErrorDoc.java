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

        docs.put("regra-de-negocio", new ErrorDocResponse(
                "Violação de regra de negócio",
                "A operação solicitada viola uma regra do sistema.",
                "Exemplos: categoria já ativa, usuário tentando agir sobre si mesmo, " +
                        "categoria já possui pai.",
                "Verifique a mensagem de erro para entender qual regra foi violada.",
                422
        ));
        docs.put("validacao", new ErrorDocResponse(
                "Validação falhou",
                "Erro de validação nos campos da requisição.",
                "Algum campo está ausente, vazio ou em formato inválido.",
                "Verifique 'fieldsErrors' na resposta e corrija os campos.",
                400
        ));
        docs.put("nome-existe", new ErrorDocResponse(
                "Nome já em uso",
                "O nome inserido já está em uso",
                "O nome inserido já foi cadastrado",
                "Caso ainda queira criar esse objeto, coloque outro nome",
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
        docs.put("categoria-nao-encontrado", new ErrorDocResponse(
                "Categoria não encontrado",
                "Não foi possível encontrar a categoria.",
                "O ID fornecido não existe.",
                "Verifique se o ID está correto.",
                404
        ));
        docs.put("fornecedor-nao-encontrado", new ErrorDocResponse(
                "Fornecedor não encontrado",
                "Não foi possível encontrar o fornecedor.",
                "O dado fornecido não existe.",
                "Verifique se o dado está correto.",
                404
        ));
        docs.put("servico-externo-indisponivel", new ErrorDocResponse(
                "Serviço externo indisponível",
                "O serviço de consulta de CEP está temporariamente indisponível.",
                "O sistema de proteção (Circuit Breaker) foi ativado para evitar lentidão devido a instabilidades no ViaCEP.",
                "Por favor, tente novamente em instantes. Se o problema persistir, insira os dados do endereço manualmente.",
                503
        ));

    }

    public  Map<String, ErrorDocResponse> findAll(){
        return Collections.unmodifiableMap(docs);
    }

    public  Mono<ErrorDocResponse> find(String erroKey){

        return Mono.justOrEmpty(docs.get(erroKey));
    }
}
