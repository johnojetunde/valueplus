package com.valueplus.domain.util;

import java.util.Set;

public class Constants {
    public static Set<String> WHITE_LISTED_AUTHORITIES_UI = Set.of(
            "CREATE_ADMIN",
            "DISABLE_PRODUCT",
            "CREATE_PRODUCT",
            "VIEW_ALL_USERS",
            "CREATE_SUPER_AGENT",
            "VIEW_SUPER_AGENTS",
            "UPDATE_PRODUCT",
            "VIEW_ADMIN_WALLET_HISTORY",
            "ENABLE_PRODUCT",
            "UPDATE_PRODUCT_ORDER_STATUS",
            "UPDATE_ADMIN_AUTHORITY",
            "VIEW_ALL_WALLET",
            "VIEW_ALL_TRANSACTIONS",
            "VERIFY_PENDING_TRANSACTIONS",
            "VIEW_AUDIT_LOG",
            "VIEW_SETTINGS_SCHEDULE",
            "ENABLE_USER",
            "DISABLE_USER",
            "UPDATE_SETTINGS"
    );
}
