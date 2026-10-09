package com.credit.model;

import java.math.BigDecimal;

public enum VehicleType {
    MOBIL("Mobil", new BigDecimal("8")),
    MOTOR("Motor", new BigDecimal("9"));

    private final String label;
    private final BigDecimal baseRatePercent;

    VehicleType(String label, BigDecimal baseRatePercent) {
        this.label = label;
        this.baseRatePercent = baseRatePercent;
    }

    public String label() {
        return label;
    }

    public BigDecimal baseRatePercent() {
        return baseRatePercent;
    }
}
