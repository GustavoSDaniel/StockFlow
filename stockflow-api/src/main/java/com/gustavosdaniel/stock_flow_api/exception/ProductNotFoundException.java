package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a requested product cannot be found in the database.
 */
public class ProductNotFoundException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public ProductNotFoundException() {
    }

    /**
     * @param message the detail message
     */
    public ProductNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public ProductNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public ProductNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public ProductNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
