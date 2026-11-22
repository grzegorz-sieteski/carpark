package com.gs5.carpark.application;

import com.gs5.carpark.domain.CarPark;
import com.gs5.carpark.domain.ParkingCheckInUseCase;
import com.gs5.carpark.domain.Vehicle;

import java.time.Instant;
import java.util.Optional;

public final class ParkingCheckInInteractor implements ParkingCheckInUseCase {

    private final CarPark carPark;

    public ParkingCheckInInteractor(CarPark carPark) {
        this.carPark = carPark;
    }

    @Override
    public Optional<ParkingCheckResponse> execute(Vehicle vehicle) {
        return carPark.checkIn(vehicle, Instant.now())
                .map(t -> new ParkingCheckResponse(t.registrationNumber(), t.enterTime()));
    }
}
