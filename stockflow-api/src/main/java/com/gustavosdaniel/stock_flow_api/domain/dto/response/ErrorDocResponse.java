package com.gustavosdaniel.stock_flow_api.domain.dto.response;

public record ErrorDocResponse(

        String title,
        String detail,
        String cause,
        String howToSolve,
        int status
) {
}
