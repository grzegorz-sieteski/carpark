package com.gs5.carpark.domain;

import java.math.BigDecimal;
import java.time.Instant;

public record Bill(RegistrationNumber registrationNumber, BigDecimal vehicleCharge, Instant timeIn, Instant timeOut) {}
