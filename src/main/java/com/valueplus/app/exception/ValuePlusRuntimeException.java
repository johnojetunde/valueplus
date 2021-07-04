package com.valueplus.app.exception;

public class ValuePlusRuntimeException extends RuntimeException {

    public ValuePlusRuntimeException(String message) {
        super(message);
    }

    public ValuePlusRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
