package com.gustavosdaniel.stock_flow_api.domain.enums;

/**
 * Represents the lifecycle status of a product in the catalog.
 * <ul>
 *   <li>{@code ACTIVE} -- available for sale and visible in listings.</li>
 *   <li>{@code INACTIVE} -- temporarily unavailable but kept for historical records.</li>
 *   <li>{@code DISCONTINUED} -- permanently removed from the active catalog.</li>
 * </ul>
 */
public enum ProductStatus {

    ACTIVE,
    INACTIVE,
    DISCONTINUED
}
