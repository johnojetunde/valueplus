package com.valueplus.app.exception;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class ValuePlusException extends Exception {
    private final HttpStatus httpStatus;

    public ValuePlusException(String message, HttpStatus status) {
        super(message);
        this.httpStatus = status;
    }

    public ValuePlusException(String message) {
        super(message);
        this.httpStatus = INTERNAL_SERVER_ERROR;
    }

    public ValuePlusException(Throwable cause) {
        super(cause);
        this.httpStatus = INTERNAL_SERVER_ERROR;
    }

    public ValuePlusException(Throwable cause, HttpStatus status) {
        super(cause);
        this.httpStatus = status;
    }

    public ValuePlusException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = INTERNAL_SERVER_ERROR;
    }

    public ValuePlusException(String message, Throwable cause, HttpStatus status) {
        super(message, cause);
        this.httpStatus = status;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
