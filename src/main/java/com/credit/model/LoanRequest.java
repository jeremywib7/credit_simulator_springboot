package com.credit.model;

import java.math.BigDecimal;

public record LoanRequest(
        VehicleType vehicleType,
        VehicleCondition condition,
        int year,
        BigDecimal totalLoan,
        int tenorYears,
        BigDecimal downPayment) {
}
