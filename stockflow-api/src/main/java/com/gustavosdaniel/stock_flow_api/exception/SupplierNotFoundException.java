package com.gustavosdaniel.stock_flow_api.exception;

public class SupplierNotFoundException extends RuntimeException{
    public SupplierNotFoundException() {
    }

    public SupplierNotFoundException(String message) {
        super(message);
    }

    public SupplierNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SupplierNotFoundException(Throwable cause) {
        super(cause);
    }

    public SupplierNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
