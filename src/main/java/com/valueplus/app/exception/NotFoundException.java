package com.valueplus.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.HttpClientErrorException;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends HttpClientErrorException {

    public NotFoundException(String statusText) {
        super(HttpStatus.NOT_FOUND, statusText);
    }
}
