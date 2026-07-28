package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when the authenticated user does not have sufficient permissions
 * to perform the requested operation.
 */
public class UnauthorizedException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public UnauthorizedException() {
    }

    /**
     * @param message the detail message
     */
    public UnauthorizedException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public UnauthorizedException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public UnauthorizedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
