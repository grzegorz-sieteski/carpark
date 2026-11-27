package com.gs5.carpark.domain;

import java.time.Instant;
import java.util.Objects;

/**
 * A vehicle occupying a space since {@code startTime}.
 */
public record ParkingSession(Vehicle vehicle, Space space, Instant startTime) {

    public ParkingSession {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        Objects.requireNonNull(space, "Space must not be null");
        Objects.requireNonNull(startTime, "Start time must not be null");
    }

    RegistrationNumber registrationNumber() {
        return vehicle.registrationNumber();
    }

    Ticket ticket() {
        return new Ticket(space, registrationNumber(), startTime);
    }

    /**
     * Closes the session and prices it. The wall clock can step backwards between check-in and
     * check-out (NTP correction, manual change). That must neither strand the vehicle nor
     * overcharge it, so a check-out before the start is treated as a zero-length stay.
     */
    Bill billAt(Instant at, Tariff tariff) {
        final var end = at.isBefore(startTime) ? startTime : at;
        final var timeOfUsed = new TimeofUsed(startTime, end);
        final var charge = tariff.chargeFor(vehicle.type(), timeOfUsed);
        return new Bill(registrationNumber(), charge.toPounds(), timeOfUsed.start(), timeOfUsed.end());
    }
}
