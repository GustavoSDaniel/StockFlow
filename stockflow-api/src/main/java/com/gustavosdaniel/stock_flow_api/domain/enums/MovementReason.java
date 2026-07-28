package com.gustavosdaniel.stock_flow_api.domain.enums;

/**
 * Enumerates the possible reasons for an inventory movement.
 * <p>
 * Used in conjunction with {@link MovementType} to provide detailed context
 * for each stock change (e.g., a SALE exit vs. a LOSS exit).
 * </p>
 *
 * @see MovementType
 */
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
