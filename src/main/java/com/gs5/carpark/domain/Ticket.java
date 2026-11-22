package com.gs5.carpark.domain;

import java.time.Instant;

public record Ticket(Space position, RegistrationNumber registrationNumber, Instant enterTime) {
}
