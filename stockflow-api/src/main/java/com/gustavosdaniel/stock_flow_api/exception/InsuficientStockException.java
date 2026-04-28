package com.gustavosdaniel.stock_flow_api.exception;

public class InsuficientStockException extends RuntimeException{

    public InsuficientStockException() {
    }

    public InsuficientStockException(String message) {
        super(message);
    }

    public InsuficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsuficientStockException(Throwable cause) {
        super(cause);
    }

    public InsuficientStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
