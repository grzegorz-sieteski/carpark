package com.gs5.carpark.application;

import com.gs5.carpark.domain.*;

import java.time.Instant;
import java.util.Optional;

public final class CheckOutInteractor implements ParkingCheckOutUseCase {

    private final CarPark carPark;

    public CheckOutInteractor(CarPark carPark) {
        this.carPark = carPark;
    }

    @Override
    public Optional<ParkingCheckOutResponse> execute(RegistrationNumber registrationNumber) {
        return carPark.checkOut(registrationNumber, Instant.now())
                .map(ParkingCheckOutResponse::new);
    }
}
