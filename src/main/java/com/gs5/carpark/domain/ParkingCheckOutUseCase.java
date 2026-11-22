package com.gs5.carpark.domain;


import java.util.Optional;

public interface ParkingCheckOutUseCase {
    Optional<ParkingCheckOutResponse> execute(RegistrationNumber registrationNumber);

    record ParkingCheckOutResponse(Bill bill) {}

}
