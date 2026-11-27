package com.gs5.carpark.domain;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Aggregate root: guarantees that a vehicle is parked at most once and that a space holds at
 * most one vehicle. Every state change goes through a single lock.
 */
public final class CarPark {

    private static final int CAPACITY = 2;

    private final ReentrantLock lock = new ReentrantLock();
    private final Map<RegistrationNumber, ParkingSession> activeSessions = new HashMap<>();
    private final ParkingSpaces spaces;
    private final Tariff tariff;

    public CarPark() {
        this(new ParkingSpaces(CAPACITY), new Tariff());
    }

    CarPark(ParkingSpaces spaces, Tariff tariff) {
        this.spaces = spaces;
        this.tariff = tariff;
    }

    public Occupancy occupancy() {
        lock.lock();
        try {
            return new Occupancy(spaces.freeCount(), activeSessions.size());
        } finally {
            lock.unlock();
        }
    }

    public Optional<Ticket> checkIn(Vehicle vehicle, Instant startTime) {
        Objects.requireNonNull(vehicle, "Vehicle must not be null");
        Objects.requireNonNull(startTime, "Start time must not be null");
        lock.lock();
        try {
            if (activeSessions.containsKey(vehicle.registrationNumber())) {
                return Optional.empty();
            }
            return spaces.allocate()
                    .map(space -> startSession(vehicle, space, startTime));
        } finally {
            lock.unlock();
        }
    }

    public Optional<Bill> checkOut(RegistrationNumber registrationNumber, Instant at) {
        Objects.requireNonNull(registrationNumber, "Registration number must not be null");
        Objects.requireNonNull(at, "Check-out time must not be null");
        final ParkingSession session;
        lock.lock();
        try {
            session = activeSessions.remove(registrationNumber);
            if (session == null) {
                return Optional.empty();
            }
            spaces.release(session.space());
        } finally {
            lock.unlock();
        }
        return Optional.of(session.billAt(at, tariff));
    }

    private Ticket startSession(Vehicle vehicle, Space space, Instant startTime) {
        final var session = new ParkingSession(vehicle, space, startTime);
        activeSessions.put(session.registrationNumber(), session);
        return session.ticket();
    }
}
