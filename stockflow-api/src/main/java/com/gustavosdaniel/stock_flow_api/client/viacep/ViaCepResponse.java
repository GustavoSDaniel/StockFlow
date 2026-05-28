package com.gustavosdaniel.stock_flow_api.client.viacep;

public record ViaCepResponse(

        String cep,
        String logradouro,
        String complement,
        String bairro,
        String localidade,
        String uf,
        boolean erro
) {
}
