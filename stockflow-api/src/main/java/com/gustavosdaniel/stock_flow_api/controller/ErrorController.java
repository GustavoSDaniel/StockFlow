package com.gustavosdaniel.stock_flow_api.controller;

import com.gustavosdaniel.stock_flow_api.controller.OpenApi.ErrorOpenApi;
import com.gustavosdaniel.stock_flow_api.domain.dto.response.ErrorDocResponse;
import com.gustavosdaniel.stock_flow_api.util.ErrorDoc;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/erros")
public class ErrorController implements ErrorOpenApi {

    private final ErrorDoc errorDoc;

    public ErrorController(ErrorDoc errorDoc) {
        this.errorDoc = errorDoc;
    }

    @GetMapping()
    public Mono<ResponseEntity<Map<String, ErrorDocResponse>>> getAllErrorDoc(){

        return Mono.just(ResponseEntity.ok(errorDoc.findAll()));
    }

    @GetMapping("/{errorKey}")
    public Mono<ResponseEntity<ErrorDocResponse>> getErrorDoc(@PathVariable String errorKey){

        return errorDoc.find(errorKey)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
