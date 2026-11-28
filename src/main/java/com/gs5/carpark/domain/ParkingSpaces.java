package com.gs5.carpark.domain;

import java.util.ArrayDeque;
import java.util.Optional;
import java.util.Queue;
import java.util.stream.LongStream;

/**
 * The pool of spaces of a car park. Not thread-safe: {@link CarPark} guards it.
 */
final class ParkingSpaces {

    private final Queue<Space> free = new ArrayDeque<>();

    ParkingSpaces(int capacity) {
        if (capacity < 1) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        LongStream.rangeClosed(1, capacity).mapToObj(Space::new).forEach(free::add);
    }

    Optional<Space> allocate() {
        return Optional.ofNullable(free.poll());
    }

    void release(Space space) {
        free.add(space);
    }

    int freeCount() {
        return free.size();
    }
}
