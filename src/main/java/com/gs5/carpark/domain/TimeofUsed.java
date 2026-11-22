package com.gs5.carpark.domain;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

public record TimeofUsed(Instant start, Instant end) {

    public TimeofUsed {
        Objects.requireNonNull(start, "Start time must not be null");
        Objects.requireNonNull(end, "End time must not be null");
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("End time must not be before start time");
        }
    }

    public long toMinutes() {
        final var timeofUsed = Duration.between(start, end);
        return timeofUsed.getSeconds() / 60;
    }

    public Duration toDuration() {
        return Duration.between(start, end);
    }
}
