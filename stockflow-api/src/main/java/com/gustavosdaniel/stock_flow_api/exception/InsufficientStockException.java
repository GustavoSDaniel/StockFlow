package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a stock operation (entry or exit) would result in a negative quantity.
 * <p>
 * This guards against over-dispatching inventory and is raised before the
 * movement is persisted, resulting in an HTTP 400 response.
 * </p>
 */
public class InsufficientStockException extends RuntimeException{

    /**
     * Constructs an exception with a default Portuguese detail message indicating
     * insufficient stock.
     */
    public InsufficientStockException() {
        super("Estoque insuficiente para realizar a operação");
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public InsufficientStockException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified cause.
     *
     * @param cause the cause
     */
    public InsufficientStockException(Throwable cause) {
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
    public InsufficientStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
