package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a requested stock record cannot be found in the database.
 */
public class StockNotFoundException extends RuntimeException{

    /** Constructs an exception with the default detail message "Estoque não encontrado". */
    public StockNotFoundException() {
        super("Estoque não encontrado");
    }

    /**
     * @param message the detail message
     */
    public StockNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public StockNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public StockNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public StockNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
