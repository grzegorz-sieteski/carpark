package com.gs5.carpark.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TimeofUsedTest {

    private static final Instant START = Instant.parse("2026-01-01T10:00:00Z");

    @Test
    void shouldRejectEndBeforeStart() {
        assertThatThrownBy(() -> new TimeofUsed(START, START.minusSeconds(1)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must not be before start time");
    }

    @Test
    void shouldRejectMissingStart() {
        assertThatThrownBy(() -> new TimeofUsed(null, START))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectMissingEnd() {
        assertThatThrownBy(() -> new TimeofUsed(START, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldAllowZeroDuration() {
        assertThat(new TimeofUsed(START, START).toMinutes()).isZero();
    }
}
