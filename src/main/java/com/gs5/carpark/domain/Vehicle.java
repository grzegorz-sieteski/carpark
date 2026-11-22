package com.gs5.carpark.domain;

import java.util.Objects;

public record Vehicle(RegistrationNumber registrationNumber, VehicleType type) {

    public Vehicle {
        Objects.requireNonNull(registrationNumber, "Registration number must not be null");
        Objects.requireNonNull(type, "Vehicle type must not be null");
    }

    public static Vehicle of(RegistrationNumber number, int typeCode) {
        return new Vehicle(number, VehicleType.fromCode(typeCode));
    }

    public static Vehicle small(RegistrationNumber number) {
        return new Vehicle(number, VehicleType.SMALL);
    }

    public static Vehicle medium(RegistrationNumber number) {
        return new Vehicle(number, VehicleType.MEDIUM);
    }

    public static Vehicle large(RegistrationNumber number) {
        return new Vehicle(number, VehicleType.LARGE);
    }

    public enum VehicleType {
        SMALL(1),
        MEDIUM(2),
        LARGE(3);

        private final int code;

        VehicleType(int code) {
            this.code = code;
        }

        public int code() {
            return code;
        }

        public static VehicleType fromCode(int code) {
            return switch (code) {
                case 1 -> SMALL;
                case 2 -> MEDIUM;
                case 3 -> LARGE;
                default -> throw new IllegalArgumentException("Unknown vehicle type code: " + code);
            };
        }
    }

}
