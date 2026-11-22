package com.gs5.carpark.domain;

import java.math.BigDecimal;

public record Pence(long amount) {

    public BigDecimal toPounds() {
        return BigDecimal.valueOf(amount, 2);
    }
}
