package com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard;

import java.math.BigDecimal;

public record DashboardOverviewResponse(

        ProductStats products,
        FinancialStats financials,
        Integer totalSuppliers,
        Integer totalCategories,
        Integer totalNotifications
) {

    public record ProductStats(

            Long total,
            Long active,
            Long inactive,
            Long discontinued
    ){}

    public record FinancialStats(

            BigDecimal totalStockValue,
            BigDecimal potentialSalesValue,
            BigDecimal averageMarginPercentage
    ){}
}
