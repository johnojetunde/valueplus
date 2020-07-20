package com.codeemma.valueplus.paystack.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ResponseModel {
    private boolean status;
    private String message;
    private Object data;
}
