package com.home365.jobservice.utils;

import java.util.Objects;

public abstract class CurrencyConverter {
    public static long toDollar(double value) {
        return (long) (value / 100);
    }

    public static Double toDollar(long value) {
        return Double.valueOf(value) / 100;
    }

    public static Double toDollar(Double value) {
        return value/ 100d;
    }
    public static long toCents(Double value) {
        return (long) (value * 100);
    }

    public static Long toCents(Long value) {
        if (Objects.nonNull(value))
            return value * 100;
        return null;
    }
}
