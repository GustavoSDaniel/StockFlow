package com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record DashboardSupplierResponse(

        List<SupplierDashboardItem> suppliers
) {
    public record SupplierDashboardItem(

            UUID id,
            String name,
            Long totalProducts,
            BigDecimal totalStockValue
    ){}
}
