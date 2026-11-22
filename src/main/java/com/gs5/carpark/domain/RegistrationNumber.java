package com.gs5.carpark.domain;

public record RegistrationNumber(String number) {

    public RegistrationNumber {
        if (number == null || number.isBlank()) {
            throw new IllegalArgumentException("Registration number must not be blank");
        }
    }
}
