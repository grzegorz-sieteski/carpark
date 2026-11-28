package com.gs5.carpark.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class CarParkConcurrencyTest {

    private static final Instant NOW = Instant.parse("2026-01-01T10:00:00Z");
    private static final int TIMEOUT_SECONDS = 30;

    private final CarPark carPark = new CarPark();

    @Test
    void shouldAdmitExactlyCapacityWhenManyDistinctVehiclesArrive() throws Exception {
        final var successes = runConcurrently(64, i ->
                carPark.checkIn(Vehicle.small(new RegistrationNumber("REG " + i)), NOW).isPresent());

        assertThat(successes).isEqualTo(2);
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(0, 2));
    }

    @Test
    void shouldAdmitSameRegistrationOnlyOnce() throws Exception {
        final var successes = runConcurrently(64, i ->
                carPark.checkIn(Vehicle.small(new RegistrationNumber("LON 11111")), NOW).isPresent());

        assertThat(successes).isEqualTo(1);
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(1, 1));
    }

    @Test
    void shouldKeepInvariantUnderCheckInCheckOutChurn() throws Exception {
        final var stop = new AtomicBoolean();
        final var violation = new AtomicReference<Occupancy>();
        final var observer = new Thread(() -> {
            while (!stop.get()) {
                final var o = carPark.occupancy();
                if (o.availableSpaces() + o.occupiedSpaces() != 2) {
                    violation.set(o);
                }
            }
        });
        observer.start();
        try {
            runConcurrently(16, i -> {
                final var reg = new RegistrationNumber("REG " + i);
                for (int n = 0; n < 200; n++) {
                    if (carPark.checkIn(Vehicle.small(reg), NOW).isPresent()) {
                        carPark.checkOut(reg, NOW.plusSeconds(1));
                    }
                }
                return true;
            });
        } finally {
            stop.set(true);
            observer.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
        }

        assertThat(violation.get()).isNull();
        assertThat(carPark.occupancy()).isEqualTo(new Occupancy(2, 0));
    }

    private static long runConcurrently(int threads, Task task) throws Exception {
        final var pool = Executors.newFixedThreadPool(threads);
        try {
            final var ready = new CountDownLatch(threads);
            final var start = new CountDownLatch(1);
            final List<Future<Boolean>> futures = new ArrayList<>();
            for (int i = 0; i < threads; i++) {
                final int index = i;
                futures.add(pool.submit(() -> {
                    ready.countDown();
                    start.await();
                    return task.run(index);
                }));
            }
            assertThat(ready.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            long successes = 0;
            for (var f : futures) {
                if (f.get(TIMEOUT_SECONDS, TimeUnit.SECONDS)) {
                    successes++;
                }
            }
            return successes;
        } finally {
            pool.shutdownNow();
        }
    }

    @FunctionalInterface
    private interface Task {
        boolean run(int index) throws Exception;
    }
}
