package com.codeemma.valueplus.domain.service;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.paystack.model.TransferVerificationResponse;

import java.math.BigDecimal;

public interface PaymentService {
    TransferResponse transfer(String accountNumber,
                              String bankCode,
                              BigDecimal amount) throws ValuePlusException;

    TransferVerificationResponse verifyTransfer(String reference) throws ValuePlusException;
}
