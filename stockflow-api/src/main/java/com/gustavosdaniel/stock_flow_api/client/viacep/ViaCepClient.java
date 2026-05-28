package com.gustavosdaniel.stock_flow_api.client.viacep;

import com.gustavosdaniel.stock_flow_api.exception.BusinessRuleException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ViaCepClient {

    private final WebClient webClient;
    private final Logger log = LoggerFactory.getLogger(ViaCepClient.class);


    public ViaCepClient(WebClient.Builder builder) {
        this.webClient = builder
                .baseUrl("https://viacep.com.br/ws")
                .build();
    }

    public Mono<ViaCepResponse> findByAddressByZipCode(String zipCode){

        return webClient.get()
                .uri("/{zipCode}/json", zipCode)
                .retrieve()
                .onStatus(
                        HttpStatusCode::isError,
                        response ->Mono.error(new BusinessRuleException(
                                "Erro ao consultar o ViaCEP"
                        ))
                )
                .bodyToMono(ViaCepResponse.class)
                .filter(response -> !response.erro())
                .switchIfEmpty(Mono.error(new BusinessRuleException(
                        "CEP não encontrado: " + zipCode
                )))
                .timeout(Duration.ofSeconds(5))
                .doOnNext( r -> log.info(
                        "CEP {} encontrado via ViaCEP com sucesso: {}", zipCode, r.localidade()))
                .doOnError(e -> log.warn(
                        "ViaCEP indisponível para o CEP {}: {}", zipCode, e.getMessage()
                ));
    }

}
