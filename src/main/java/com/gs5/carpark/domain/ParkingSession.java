package com.gs5.carpark.domain;

import java.time.Instant;

public record ParkingSession(Vehicle vehicle, Space space, Instant startTime) {
}
