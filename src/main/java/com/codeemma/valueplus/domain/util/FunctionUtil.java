package com.codeemma.valueplus.domain.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

@UtilityClass
public class FunctionUtil {
    private static final BigDecimal MULTIPLIER = BigDecimal.valueOf(100.00);

    public static BigDecimal convertToKobo(BigDecimal amount) {
        return amount.multiply(MULTIPLIER);
    }

    public static BigDecimal convertToNaira(BigDecimal amount) {
        return amount.divide(MULTIPLIER, RoundingMode.UNNECESSARY);
    }

}
