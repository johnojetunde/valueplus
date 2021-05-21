package com.valueplus.fixtures;

import com.valueplus.app.model.PaymentRequestModel;
import com.valueplus.domain.enums.TransactionType;
import com.valueplus.domain.model.PinUpdate;
import com.valueplus.domain.model.RoleType;
import com.valueplus.paystack.model.AccountNumberModel;
import com.valueplus.paystack.model.TransferResponse;
import com.valueplus.persistence.entity.*;
import lombok.experimental.UtilityClass;

import java.math.BigDecimal;

import static com.valueplus.domain.model.RoleType.AGENT;

@UtilityClass
public class TestFixtures {
    public static User mockUser() {
        return getUser(AGENT);
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

    public static PinUpdate pinUpdate() {
        return new PinUpdate("password", "1345", "1234");
    }

    public static ProductOrder productOrder(User user, BigDecimal sellingPrice, Product product) {
        return ProductOrder.builder()
                .id(1L)
                .quantity(1L)
                .product(product)
                .sellingPrice(sellingPrice)
                .user(user)
                .build();
    }

    public static Product product(BigDecimal price) {
        return Product.builder()
                .id(1L)
                .price(price)
                .build();
    }

    public static AccountNumberModel mockAccountNumberModel(String accountNumber) {
        return new AccountNumberModel(accountNumber, "Value Plus", 1L);
    }

    public static PaymentRequestModel mockPaymentRequestModel(BigDecimal amount, String pin) {
        return new PaymentRequestModel(amount, pin);
    }

    public static TransferResponse mockTransferResponse(BigDecimal amount) {
        return TransferResponse.builder()
                .amount(amount)
                .reference("12232324242")
                .status("otp")
                .build();
    }

    public static Transaction mockTransaction(String accountNumber) {
        return mockTransaction(mockUser(), accountNumber, BigDecimal.ONE, "otp");
    }

    public static Transaction mockTransaction(User user,
                                              String accountNumber,
                                              BigDecimal amount,
                                              String status) {
        return Transaction.builder()
                .id(1L)
                .amount(amount)
                .reference("12232324242")
                .status(status)
                .accountNumber(accountNumber)
                .currency("NGN")
                .bankCode("044")
                .user(user)
                .build();
    }

    public static User getUser(RoleType roleType) {
        return User.builder()
                .id(1L)
                .role(new Role(1L, roleType.name()))
                .agentCode("agent12244")
                .isTransactionTokenSet(true)
                .transactionPin("gdhgvhgerbdugdfudgfduyf")
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
