package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a domain business rule is violated.
 * <p>
 * Typical scenarios include: attempting to disable an already-inactive entity,
 * changing a stock level that violates inventory constraints, or performing
 * an operation that contradicts the aggregate's invariants.
 * </p>
 */
public class BusinessRuleException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public BusinessRuleException() {
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public BusinessRuleException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public BusinessRuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified cause.
     *
     * @param cause the cause
     */
    public BusinessRuleException(Throwable cause) {
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
    public BusinessRuleException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
