package com.credit.model;

import java.math.BigDecimal;

public record YearlyInstallment(int year, BigDecimal monthlyAmount, BigDecimal ratePercent) {
}
