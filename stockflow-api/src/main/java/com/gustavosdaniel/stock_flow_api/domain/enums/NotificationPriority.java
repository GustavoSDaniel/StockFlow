package com.gustavosdaniel.stock_flow_api.domain.enums;

/**
 * Defines the priority levels for system notifications.
 * <p>
 * Used to classify the urgency of alerts displayed to users, ranging from
 * {@code LOW} to {@code CRITICAL}.
 * </p>
 */
public enum NotificationPriority {

    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}
