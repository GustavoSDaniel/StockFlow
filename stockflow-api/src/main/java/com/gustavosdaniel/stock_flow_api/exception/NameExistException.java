package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when attempting to create or update an entity with a name that already
 * exists in the database, violating a uniqueness constraint.
 */
public class NameExistException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public NameExistException() {
    }

    /**
     * Constructs an exception with the specified detail message.
     *
     * @param message the detail message
     */
    public NameExistException(String message) {
        super(message);
    }

    /**
     * Constructs an exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause   the cause
     */
    public NameExistException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an exception with the specified cause.
     *
     * @param cause the cause
     */
    public NameExistException(Throwable cause) {
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
    public NameExistException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
