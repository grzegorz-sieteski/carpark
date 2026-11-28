package com.gs5.carpark.application;

import com.gs5.carpark.domain.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class ParkingInteractorsTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final RegistrationNumber REGISTRATION = new RegistrationNumber("LON 11111");

    private final CarPark carPark = new CarPark();

    private static Clock at(Instant instant) {
        return Clock.fixed(instant, ZoneOffset.UTC);
    }

    @Test
    void shouldStampCheckInTimeFromClock() {
        final var checkIn = new ParkingCheckInInteractor(carPark, at(NOW));

        final var response = checkIn.execute(Vehicle.small(REGISTRATION));

        assertThat(response).map(ParkingCheckInUseCase.ParkingCheckResponse::timeIn).contains(NOW);
    }

    @Test
    void shouldBillCheckOutUsingClock() {
        new ParkingCheckInInteractor(carPark, at(NOW)).execute(Vehicle.small(REGISTRATION));
        final var checkOut = new CheckOutInteractor(carPark, at(NOW.plus(Duration.ofMinutes(10))));

        final var response = checkOut.execute(REGISTRATION);

        assertThat(response).map(r -> r.bill().vehicleCharge()).contains(new BigDecimal("12.00"));
    }

    @Test
    void shouldBillZeroAndReleaseSpaceWhenClockStepsBack() {
        new ParkingCheckInInteractor(carPark, at(NOW)).execute(Vehicle.small(REGISTRATION));
        final var checkOut = new CheckOutInteractor(carPark, at(NOW.minusSeconds(30)));

        final var response = checkOut.execute(REGISTRATION);

        assertThat(response).map(r -> r.bill().vehicleCharge()).contains(new BigDecimal("0.00"));
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(2, 0));
    }

    @Test
    void shouldReturnEmptyForUnknownVehicle() {
        assertThat(new CheckOutInteractor(carPark, at(NOW)).execute(REGISTRATION)).isEmpty();
    }

    @Test
    void shouldReportOccupancyAfterCheckIn() {
        new ParkingCheckInInteractor(carPark, at(NOW)).execute(Vehicle.small(REGISTRATION));

        assertThat(new GetOccupancyInteractor(carPark).get()).isEqualTo(new Occupancy(1, 1));
    }
}
