package com.valueplus.domain.enums;

public enum TransactionStatus {
    PENDING, COMPLETED, FAILED;

    public static TransactionStatus resolve(String st) {
        TransactionStatus status = PENDING;
        if ("failed".equalsIgnoreCase(st) || "error".equalsIgnoreCase(st)) {
            status = FAILED;
        } else if ("success".equalsIgnoreCase(st)) {
            status = COMPLETED;
        }

        return status;
    }
}
