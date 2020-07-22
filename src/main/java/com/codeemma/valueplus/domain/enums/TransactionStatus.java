package com.codeemma.valueplus.domain.enums;

public enum TransactionStatus {
    PENDING, COMPLETED, FAILED;

    public static TransactionStatus resolve(String st) {
        TransactionStatus status = TransactionStatus.PENDING;
        if ("failed".equalsIgnoreCase(st) || "error".equalsIgnoreCase(st)) {
            status = TransactionStatus.FAILED;
        } else if ("success".equalsIgnoreCase(st)) {
            status = TransactionStatus.COMPLETED;
        }

        return status;
    }
}
