package com.codeemma.valueplus.fixtures;

import com.codeemma.valueplus.app.model.PaymentRequestModel;
import com.codeemma.valueplus.domain.enums.TransactionType;
import com.codeemma.valueplus.domain.model.RoleType;
import com.codeemma.valueplus.paystack.model.AccountNumberModel;
import com.codeemma.valueplus.paystack.model.TransferResponse;
import com.codeemma.valueplus.persistence.entity.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

@UtilityClass
public class TestFixtures {
    public static User mockUser() {
        return getUser(RoleType.AGENT);
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

    public static User getUser(RoleType roleType) {
        return User.builder()
                .id(1L)
                .role(new Role(1L, roleType.name()))
                .build();
    }

    public static Wallet getWallet(User user) {
        return Wallet.builder()
                .id(1L)
                .amount(BigDecimal.ZERO)
                .user(user)
                .build();
    }

    public static WalletHistory getWalletHistory(Wallet wallet, TransactionType type) {
        return WalletHistory.builder()
                .id(1L)
                .type(type)
                .wallet(wallet)
                .amount(BigDecimal.ZERO)
                .build();
    }

}
