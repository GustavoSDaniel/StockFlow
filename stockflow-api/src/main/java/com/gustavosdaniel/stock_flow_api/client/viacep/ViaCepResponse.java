package com.gustavosdaniel.stock_flow_api.client.viacep;

/**
 * Holds the response data returned by the ViaCEP API for a ZIP code lookup.
 * <p>
 * The {@code erro} field is {@code true} when the CEP is not found.
 * </p>
 */
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
