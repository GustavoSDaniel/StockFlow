package com.gustavosdaniel.stock_flow_api.domain.enums;

/**
 * Represents the current condition of a product's stock level.
 * <p>
 * Determined by {@link com.gustavosdaniel.stock_flow_api.domain.po.Stock#getStockStatus()}
 * based on the current quantity relative to the configured minimum, maximum,
 * and reorder point thresholds.
 * </p>
 */
public enum StockStatus {

    OUT_OF_STOCK,
    LOW,
    REORDER_POINT,
    NORMAL,
    OVER_STOCKED
}
