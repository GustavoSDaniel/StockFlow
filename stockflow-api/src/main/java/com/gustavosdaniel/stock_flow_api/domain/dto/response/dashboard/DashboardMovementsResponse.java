package com.gustavosdaniel.stock_flow_api.domain.dto.response.dashboard;

import com.gustavosdaniel.stock_flow_api.domain.enums.MovementReason;
import com.gustavosdaniel.stock_flow_api.domain.enums.MovementType;

import java.util.List;
import java.util.UUID;

public record DashboardMovementsResponse(

        MovimentsQuantitys movimentsQuantiys,
        MovimentsType movimentsType,
        List<MovimentsMore> movimentsMores
) {

    public record MovimentsQuantitys(

            Long fullMovimentsToday,
            Long entryMovimentsMes,
            Long exitMovimentsMes
    ){}

    public record MovimentsType(

            MovementReason movementReason,
            MovementType movementType,
            Long hitoryLastryDays
    ){}

    public record MovimentsMore(

            UUID productId,
            String productName,
            String sku,
            Integer quantity
    ){}
}
