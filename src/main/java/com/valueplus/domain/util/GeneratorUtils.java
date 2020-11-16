package com.valueplus.domain.util;

import java.security.SecureRandom;

public final class GeneratorUtils {

    private GeneratorUtils() {
    }

    public static String generateRandomString(int length) {
        SecureRandom random = new SecureRandom();
        String pattern = "abcdefghjkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

        StringBuilder sb = new StringBuilder();

        while (length > 0) {
            length--;
            sb.append(pattern.charAt(random.nextInt(pattern.length())));
        }

        return sb.toString();
    }
}
