package com.gustavosdaniel.stock_flow_api.domain.enums;

public enum MovementReason {

    PURCHASE,
    RETURN_CUSTOMER,
    WARRANTY_REPLACEMENT,

    SALE,
    PROMOTIONAL_GIFT,
    INTERNAL_USE,
    QUALITY_CHECK,
    RETURN_SUPPLIER,

    INVENTORY_COUNT,
    LOSS,
    THEFT,
    DAMAGE,
    EXPIRATION,

    TRANSFER
}
