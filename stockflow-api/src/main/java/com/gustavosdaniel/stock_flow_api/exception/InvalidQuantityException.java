package com.gustavosdaniel.stock_flow_api.exception;

public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException() {
    }

    public InvalidQuantityException(String message) {
        super(message);
    }

    public InvalidQuantityException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidQuantityException(Throwable cause) {
        super(cause);
    }

    public InvalidQuantityException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
