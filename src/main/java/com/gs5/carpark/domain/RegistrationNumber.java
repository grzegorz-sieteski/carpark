package com.gs5.carpark.domain;

import java.util.Locale;

/**
 * A vehicle registration number in its canonical form: upper case, no leading or trailing
 * whitespace and every run of whitespace collapsed to a single space. This way
 * {@code "lon  11111"} and {@code "LON 11111"} are the same vehicle.
 */
public record RegistrationNumber(String number) {

    public RegistrationNumber {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Registration number must not be blank");
        }
        number = normalize(number);
    }

    private static String normalize(String number) {
        return number.replaceAll("(?U)\\s+", " ").strip().toUpperCase(Locale.ROOT);
    }
}
