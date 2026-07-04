package com.gustavosdaniel.stock_flow_api.exception;

public class NotificationNotFoundException extends RuntimeException{

    public NotificationNotFoundException() {
    }

    public NotificationNotFoundException(String message) {
        super(message);
    }

    public NotificationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotificationNotFoundException(Throwable cause) {
        super(cause);
    }

    public NotificationNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
