package com.codeemma.valueplus.domain.service.abstracts;

import com.codeemma.valueplus.app.exception.ValuePlusException;
import com.codeemma.valueplus.domain.model.AccountModel;
import com.codeemma.valueplus.paystack.model.AccountNumberModel;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.paystack.model.TransferVerificationResponse;

import java.math.BigDecimal;

public interface PaymentService {
    TransferResponse transfer(String accountNumber,
                              String bankCode,
                              BigDecimal amount) throws ValuePlusException;

    TransferResponse transfer(AccountModel accountModel,
                              BigDecimal amount) throws ValuePlusException;

    AccountNumberModel resolveAccountNumber(String accountNumber,
                                            String bankCode) throws ValuePlusException;

    TransferVerificationResponse verifyTransfer(String reference) throws ValuePlusException;
}
