package com.codeemma.valueplus.domain.enums;

public enum TransactionStatusFilter {
    error("error"),
    failed("failed"),
    otp("otp"),
    success("success"),
    pending("pending"),
    other("other");
    private final String status;

    TransactionStatusFilter(String status) {
        this.status = status;
    }

    public static TransactionStatusFilter fromString(String status) {
        for (TransactionStatusFilter b : TransactionStatusFilter.values()) {
            if (b.status.equalsIgnoreCase(status)) {
                return b;
            }
        }
        return TransactionStatusFilter.other;
    }
}
