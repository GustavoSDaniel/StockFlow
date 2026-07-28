package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a requested category cannot be found in the database.
 * <p>
 * Typically raised by service-layer lookups by ID or name before
 * returning an HTTP 404 response to the client.
 * </p>
 */
public class CategoryNotFoundException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public CategoryNotFoundException() {
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public CategoryNotFoundException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public CategoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified cause.
     *
     * @param cause the cause
     */
    public CategoryNotFoundException(Throwable cause) {
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
    public CategoryNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
