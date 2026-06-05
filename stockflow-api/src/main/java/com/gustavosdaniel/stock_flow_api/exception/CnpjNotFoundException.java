package com.gustavosdaniel.stock_flow_api.exception;

public class CnpjNotFoundException extends RuntimeException{
    public CnpjNotFoundException() {
    }

    public CnpjNotFoundException(String message) {
        super(message);
    }

    public CnpjNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public CnpjNotFoundException(Throwable cause) {
        super(cause);
    }

    public CnpjNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
