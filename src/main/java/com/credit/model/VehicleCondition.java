package com.credit.model;

import java.math.BigDecimal;

public enum VehicleCondition {
    BARU("Baru", new BigDecimal("35")),
    BEKAS("Bekas", new BigDecimal("25"));

    private final String label;
    private final BigDecimal minDownPaymentPercent;

    VehicleCondition(String label, BigDecimal minDownPaymentPercent) {
        this.label = label;
        this.minDownPaymentPercent = minDownPaymentPercent;
    }

    public String label() {
        return label;
    }

    public BigDecimal minDownPaymentPercent() {
        return minDownPaymentPercent;
    }
}
