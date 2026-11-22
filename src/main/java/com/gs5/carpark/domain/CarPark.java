package com.gs5.carpark.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public final class CarPark {

    private static final Duration EXTRA_FEE_INTERVAL = Duration.ofMinutes(5);
    private static final long EXTRA_FEE_IN_PENCE = 100L;

    private final ReentrantLock lock = new ReentrantLock();

    private final Map<RegistrationNumber, ParkingSession> activeSessions = new HashMap<>();
    private final Queue<Space> availableSpaces = new ArrayDeque<>(List.of(new Space(1), new Space(2)));


    public Occupancy occupancy() {
        lock.lock();
        try {
            return new Occupancy(availableSpaces.size(), activeSessions.size());
        } finally {
            lock.unlock();
        }
    }

    public Optional<Ticket> checkIn(Vehicle vehicle, Instant startTime) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        Objects.requireNonNull(startTime, "Start time must not be null");
        lock.lock();
        try {
            if (isAlreadyParked(vehicle)) {
                return Optional.empty();
            }
            return allocateFreeSpace()
                    .map(space -> startSession(vehicle, space, startTime));
        } finally {
            lock.unlock();
        }
    }

    public Optional<Bill> checkOut(RegistrationNumber registrationNumber, Instant at) {
        Objects.requireNonNull(registrationNumber, "Registration number must not be null");
        Objects.requireNonNull(at, "Check-out time must not be null");
        final ParkingSession session;
        final TimeofUsed timeOfUsed;
        lock.lock();
        try {
            session = activeSessions.get(registrationNumber);
            if (session == null) {
                return Optional.empty();
            }
            timeOfUsed = new TimeofUsed(session.startTime(), at);
            activeSessions.remove(registrationNumber);
            availableSpaces.add(session.space());
        } finally {
            lock.unlock();
        }
        return Optional.of(toBill(session.vehicle(), timeOfUsed));
    }

    private boolean isAlreadyParked(Vehicle vehicle) {
        return activeSessions.containsKey(vehicle.registrationNumber());
    }

    private Optional<Space> allocateFreeSpace() {
        return Optional.ofNullable(availableSpaces.poll());
    }

    private Ticket startSession(Vehicle vehicle, Space space, Instant startTime) {
        final var registrationNumber = vehicle.registrationNumber();
        activeSessions.put(registrationNumber, new ParkingSession(vehicle, space, startTime));
        return new Ticket(space, registrationNumber, startTime);
    }

    private Bill toBill(Vehicle vehicle, TimeofUsed timeOfUsed) {
        final var charge = calculateCharge(vehicle, timeOfUsed);
        return new Bill(
                vehicle.registrationNumber(),
                charge.toPounds(),
                timeOfUsed.start(),
                timeOfUsed.end()
        );
    }

    private Pence calculateCharge(Vehicle vehicle, TimeofUsed timeOfUsed) {
        final var baseCharge = timeOfUsed.toMinutes() * ratePerMinute(vehicle);
        final var completedBlocks = timeOfUsed.toDuration().dividedBy(EXTRA_FEE_INTERVAL);
        final var extraFee = completedBlocks * EXTRA_FEE_IN_PENCE;
        return new Pence(baseCharge + extraFee);
    }

    private int ratePerMinute(Vehicle vehicle) {
        return switch (vehicle.type()) {
            case SMALL -> 100;
            case MEDIUM -> 200;
            case LARGE -> 300;
        };
    }
}
