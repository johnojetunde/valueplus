package com.valueplus.domain.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class FunctionUtil {
    private static final BigDecimal MULTIPLIER = BigDecimal.valueOf(100.00);

    public static BigDecimal convertToKobo(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP).multiply(MULTIPLIER);
    }

    public static BigDecimal convertToNaira(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP)
                .divide(MULTIPLIER, RoundingMode.HALF_UP);
    }

    public static BigDecimal setScale(BigDecimal amount) {
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}
