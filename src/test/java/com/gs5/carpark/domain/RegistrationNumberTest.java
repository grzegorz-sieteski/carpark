package com.gs5.carpark.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RegistrationNumberTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldRejectBlankNumber(String number) {
        assertThatThrownBy(() -> new RegistrationNumber(number))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Registration number must not be blank");
    }

    @Test
    void shouldKeepNumberAsGiven() {
        assertThat(new RegistrationNumber("LON 11111").number()).isEqualTo("LON 11111");
    }
}
