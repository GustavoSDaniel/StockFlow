package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a requested supplier cannot be found in the database.
 */
public class SupplierNotFoundException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public SupplierNotFoundException() {
    }

    /**
     * @param message the detail message
     */
    public SupplierNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public SupplierNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public SupplierNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
