package com.valueplus.domain.util;

import lombok.experimental.UtilityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.emptyList;

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

    public static <T> Stream<T> emptyIfNullStream(Collection<T> list) {
        return (list == null) ? Stream.empty() : list.stream();
    }

    public static <T> List<T> emptyIfNull(List<T> list) {
        return (list == null) ? emptyList() : list;
    }

    public static LocalDate toDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return (date != null) ? LocalDate.parse(date, formatter) : null;
    }
}
