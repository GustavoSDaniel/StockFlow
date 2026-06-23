package com.gustavosdaniel.stock_flow_api.exception;

public class InsufficientStockException extends RuntimeException{

    public InsufficientStockException() {
        super("Estoque insuficiente para realizar a operação");
    }

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientStockException(Throwable cause) {
        super(cause);
    }

    public InsufficientStockException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
