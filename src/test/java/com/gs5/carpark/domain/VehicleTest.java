package com.gs5.carpark.domain;

import com.gs5.carpark.domain.Vehicle.VehicleType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleTest {

    private static final RegistrationNumber REGISTRATION = new RegistrationNumber("LON 11111");

    @Test
    void shouldRejectMissingRegistrationNumber() {
        assertThatThrownBy(() -> new Vehicle(null, VehicleType.SMALL))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void shouldRejectMissingType() {
        assertThatThrownBy(() -> new Vehicle(REGISTRATION, null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 4})
    void shouldRejectUnknownTypeCode(int code) {
        assertThatThrownBy(() -> Vehicle.of(REGISTRATION, code))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void shouldCreateVehicleFromTypeCode() {
        assertThat(Vehicle.of(REGISTRATION, 2)).isEqualTo(Vehicle.medium(REGISTRATION));
    }
}
