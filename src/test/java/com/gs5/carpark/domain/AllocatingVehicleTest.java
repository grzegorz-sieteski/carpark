package com.gs5.carpark.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AllocatingVehicleTest {

    private static final RegistrationNumber REGISTRATION = new RegistrationNumber("LON 33333");
    private static final Instant ARRIVAL = Instant.parse("2026-01-01T10:00:00Z");

    private final CarPark carPark = new CarPark();

    @Test
    void shouldAllocateVehicleWhenParkGotAnAvailableSpace() {
        final var ticket = carPark.checkIn(Vehicle.large(REGISTRATION), ARRIVAL);

        assertThat(ticket).map(Ticket::registrationNumber).contains(REGISTRATION);
    }

    @Test
    void shouldNotAllocateVehicleWhenCarParkIsFull() {
        carPark.checkIn(Vehicle.small(new RegistrationNumber("LON 11111")), ARRIVAL);
        carPark.checkIn(Vehicle.small(new RegistrationNumber("LON 22222")), ARRIVAL);

        final var ticket = carPark.checkIn(Vehicle.small(REGISTRATION), ARRIVAL);

        assertThat(ticket).isEmpty();
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(0, 2));
    }

    @Test
    void shouldNotAllocateVehicleWithSameRegistrationNumber() {
        final var first = carPark.checkIn(Vehicle.large(REGISTRATION), ARRIVAL);
        final var second = carPark.checkIn(Vehicle.medium(REGISTRATION), ARRIVAL);

        assertThat(first).isPresent();
        assertThat(second).isEmpty();
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(1, 1));
    }

    @Test
    void shouldUpdateOccupancyOnCheckIn() {
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(2, 0));

        carPark.checkIn(Vehicle.medium(REGISTRATION), ARRIVAL);

        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(1, 1));
    }

    @Test
    void shouldReleaseSpaceOnCheckOut() {
        carPark.checkIn(Vehicle.medium(REGISTRATION), ARRIVAL);

        carPark.checkOut(REGISTRATION, ARRIVAL.plus(Duration.ofMinutes(10)));

        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(2, 0));
    }

    @Test
    void shouldIssueBillWithParkingDetails() {
        final var departure = ARRIVAL.plus(Duration.ofMinutes(10));
        carPark.checkIn(Vehicle.small(REGISTRATION), ARRIVAL);

        final var bill = carPark.checkOut(REGISTRATION, departure);

        assertThat(bill).contains(new Bill(REGISTRATION, new BigDecimal("12.00"), ARRIVAL, departure));
    }

    @Test
    void shouldNotIssueBillForVehicleThatIsNotParked() {
        assertThat(carPark.checkOut(REGISTRATION, ARRIVAL)).isEmpty();
    }

    @Test
    void shouldBillZeroAndReleaseSpaceWhenClockWentBackwards() {
        carPark.checkIn(Vehicle.small(REGISTRATION), ARRIVAL);

        final var bill = carPark.checkOut(REGISTRATION, ARRIVAL.minusSeconds(1));

        assertThat(bill).contains(new Bill(REGISTRATION, new BigDecimal("0.00"), ARRIVAL, ARRIVAL));
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(2, 0));
    }

    @Test
    void shouldTreatDifferentlyWrittenRegistrationAsSameVehicle() {
        carPark.checkIn(Vehicle.small(new RegistrationNumber("LON 11111")), ARRIVAL);

        final var ticket = carPark.checkIn(Vehicle.small(new RegistrationNumber("lon  11111")), ARRIVAL);

        assertThat(ticket).isEmpty();
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(1, 1));
    }

    @Test
    void shouldReuseReleasedSpace() {
        final var first = new RegistrationNumber("LON 11111");
        carPark.checkIn(Vehicle.small(first), ARRIVAL);
        carPark.checkIn(Vehicle.small(new RegistrationNumber("LON 22222")), ARRIVAL);
        carPark.checkOut(first, ARRIVAL.plusSeconds(60));

        final var ticket = carPark.checkIn(Vehicle.small(REGISTRATION), ARRIVAL.plusSeconds(61));

        assertThat(ticket).isPresent();
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(0, 2));
    }

    @ParameterizedTest
    @CsvSource({
            "small, 0, 0.00",
            "small, 59, 0.00",
            "small, 299, 4.00",
            "medium, 299, 8.00",
            "small, 300, 6.00",
            "small, 599, 10.00",
            "large, 599, 28.00",
            "small, 900, 18.00",
            "large, 900, 48.00"
    })
    void shouldChargeAtBoundariesBySeconds(String size, int seconds, BigDecimal expectedCharge) {
        carPark.checkIn(vehicle(size), ARRIVAL);

        final var bill = carPark.checkOut(REGISTRATION, ARRIVAL.plusSeconds(seconds));

        assertThat(bill).map(Bill::vehicleCharge).contains(expectedCharge);
    }

    @Test
    void shouldRejectCheckOutWithoutRegistrationNumber() {
        assertThatThrownBy(() -> carPark.checkOut(null, ARRIVAL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectCheckInWithoutVehicle() {
        assertThatThrownBy(() -> carPark.checkIn(null, ARRIVAL))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "small, 4, 4.00",
            "medium, 4, 8.00",
            "large, 4, 12.00",
            "small, 5, 6.00",
            "medium, 5, 11.00",
            "large, 5, 16.00",
            "small, 10, 12.00",
            "medium, 10, 22.00",
            "large, 10, 32.00"
    })
    void shouldChargeByVehicleSizeAndParkingTime(String size, int minutes, BigDecimal expectedCharge) {
        carPark.checkIn(vehicle(size), ARRIVAL);

        final var bill = carPark.checkOut(REGISTRATION, ARRIVAL.plus(Duration.ofMinutes(minutes)));

        assertThat(bill).map(Bill::vehicleCharge).contains(expectedCharge);
    }

    private static Vehicle vehicle(String size) {
        return switch (size) {
            case "small" -> Vehicle.small(REGISTRATION);
            case "medium" -> Vehicle.medium(REGISTRATION);
            case "large" -> Vehicle.large(REGISTRATION);
            default -> throw new IllegalArgumentException(size);
        };
    }
}
