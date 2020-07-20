package com.codeemma.valueplus.app.exception;

public class ValuePlusException extends Exception {
    public ValuePlusException(String message) {
        super(message);
    }

    public ValuePlusException(String message, Throwable cause) {
        super(message, cause);
    }
}
