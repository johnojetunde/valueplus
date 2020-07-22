package com.codeemma.valueplus.fixtures;

import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.paystack.model.AccountNumberModel;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.persistence.entity.Account;
import com.codeemma.valueplus.persistence.entity.Transaction;
import com.codeemma.valueplus.persistence.entity.User;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class TestFixtures {
    public static User mockUser() {
        return User.builder()
                .id(1L)
                .build();
    }

    public static Account mockAccount(String accountNumber) {
        return Account.builder()
                .accountName("Value Plus")
                .accountNumber(accountNumber)
                .bankCode("044")
                .id(1L)
                .user(mockUser())
                .build();
    }

    public static AccountNumberModel mockAccountNumberModel(String accountNumber) {
        return new AccountNumberModel(accountNumber, "Value Plus", 1L);
    }

    public static PaymentRequestModel mockPaymentRequestModel(BigDecimal amount) {
        return new PaymentRequestModel(amount);
    }

    public static TransferResponse mockTransferResponse(BigDecimal amount) {
        return TransferResponse.builder()
                .amount(amount)
                .reference("12232324242")
                .status("otp")
                .build();
    }

    public static Transaction mockTransaction(String accountNumber) {
        return Transaction.builder()
                .id(1L)
                .amount(BigDecimal.ONE)
                .reference("12232324242")
                .status("otp")
                .accountNumber(accountNumber)
                .currency("NGN")
                .bankCode("044")
                .user(mockUser())
                .build();
    }
}
