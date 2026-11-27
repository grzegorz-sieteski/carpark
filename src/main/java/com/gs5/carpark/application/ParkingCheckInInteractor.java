package com.gs5.carpark.application;

import com.gs5.carpark.domain.CarPark;
import com.gs5.carpark.domain.ParkingCheckInUseCase;
import com.gs5.carpark.domain.Vehicle;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class ParkingCheckInInteractor implements ParkingCheckInUseCase {

    private final CarPark carPark;
    private final Clock clock;

    public ParkingCheckInInteractor(CarPark carPark, Clock clock) {
        this.carPark = Objects.requireNonNull(carPark, "Car park must not be null");
        this.clock = Objects.requireNonNull(clock, "Clock must not be null");
    }

    @Override
    public Optional<ParkingCheckResponse> execute(Vehicle vehicle) {
        return carPark.checkIn(vehicle, Instant.now(clock))
                .map(t -> new ParkingCheckResponse(t.registrationNumber(), t.enterTime()));
    }
}
