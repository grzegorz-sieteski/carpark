package com.gs5.carpark.application;

import com.gs5.carpark.domain.*;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class CheckOutInteractor implements ParkingCheckOutUseCase {

    private final CarPark carPark;
    private final Clock clock;

    public CheckOutInteractor(CarPark carPark, Clock clock) {
        this.carPark = Objects.requireNonNull(carPark, "Car park must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public Optional<ParkingCheckOutResponse> execute(RegistrationNumber registrationNumber) {
        return carPark.checkOut(registrationNumber, Instant.now(clock))
                .map(ParkingCheckOutResponse::new);
    }
}
