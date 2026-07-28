package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a quantity value is invalid (e.g., negative, zero, or exceeds
 * a maximum threshold) for a stock or movement operation.
 */
public class InvalidQuantityException extends RuntimeException {

    /** Constructs an exception with no detail message. */
    public InvalidQuantityException() {
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InvalidQuantityException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified cause.
     *
     * @param cause the cause
     */
    public InvalidQuantityException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs an exception with full control over suppression and stack trace.
     *
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public InvalidQuantityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
