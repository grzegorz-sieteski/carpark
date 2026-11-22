package com.gs5.carpark.domain;

import java.time.Instant;
import java.util.Optional;

public interface ParkingCheckInUseCase {
    Optional<ParkingCheckResponse> execute(Vehicle domain);

     record ParkingCheckResponse(RegistrationNumber vehicleReg, Instant timeIn){}
}
