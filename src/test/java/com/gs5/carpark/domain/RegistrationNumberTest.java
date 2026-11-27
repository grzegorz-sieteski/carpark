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

    @ParameterizedTest
    @ValueSource(strings = {"LON 11111", "lon 11111", "  LON 11111  ", "LON    11111", "LON\t11111", "LON\u00A011111"})
    void shouldNormalizeNumberToCanonicalForm(String number) {
        assertThat(new RegistrationNumber(number).number()).isEqualTo("LON 11111");
    }

    @Test
    void shouldTreatDifferentlyWrittenNumbersAsEqual() {
        assertThat(new RegistrationNumber(" lon  11111")).isEqualTo(new RegistrationNumber("LON 11111"));
    }
}
