package com.gustavosdaniel.stock_flow_api.exception;

/**
 * Thrown when a requested user cannot be found in Keycloak or the local database.
 */
public class UserNotFoundException extends RuntimeException{

    /** Constructs an exception with no detail message. */
    public UserNotFoundException() {
    }

    /**
     * @param message the detail message
     */
    public UserNotFoundException(String message) {
        super(message);
    }

    /**
     * @param message the detail message
     * @param cause   the cause
     */
    public UserNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param cause the cause
     */
    public UserNotFoundException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message            the detail message
     * @param cause              the cause
     * @param enableSuppression  whether suppression is enabled or disabled
     * @param writableStackTrace whether the stack trace should be writable
     */
    public UserNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
