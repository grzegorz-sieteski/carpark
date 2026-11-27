package com.gs5.carpark.domain;

import java.time.Duration;

/**
 * Pricing policy: a per-minute rate by vehicle size (whole minutes only) plus a flat fee for
 * every full five minutes parked.
 */
final class Tariff {

    private static final Duration EXTRA_FEE_INTERVAL = Duration.ofMinutes(5);
    private static final long EXTRA_FEE_IN_PENCE = 100L;

    Pence chargeFor(Vehicle.VehicleType type, TimeofUsed timeOfUsed) {
        final long baseCharge = timeOfUsed.toMinutes() * ratePerMinute(type);
        final long completedBlocks = timeOfUsed.toDuration().dividedBy(EXTRA_FEE_INTERVAL);
        return new Pence(baseCharge + completedBlocks * EXTRA_FEE_IN_PENCE);
    }

    private static int ratePerMinute(Vehicle.VehicleType type) {
        return switch (type) {
            case SMALL -> 100;
            case MEDIUM -> 200;
            case LARGE -> 300;
        };
    }
}
