package com.credit.service;

import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import com.credit.model.YearlyInstallment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LoanCalculatorTest {

    private final LoanCalculator calculator = new LoanCalculator();

    private LoanRequest request(VehicleType type, int tenor, String total, String dp) {
        return new LoanRequest(type, VehicleCondition.BARU, 2025, new BigDecimal(total), tenor, new BigDecimal(dp));
    }

    @Test
    void mobilRatesFollowYearOnYearRule() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 6, "1000000000", "500000000"));
        assertEquals(6, r.size());
        assertEquals(0, new BigDecimal("8").compareTo(r.get(0).ratePercent()));
        assertEquals(0, new BigDecimal("8.1").compareTo(r.get(1).ratePercent()));
        assertEquals(0, new BigDecimal("8.6").compareTo(r.get(2).ratePercent()));
        assertEquals(0, new BigDecimal("8.7").compareTo(r.get(3).ratePercent()));
        assertEquals(0, new BigDecimal("9.2").compareTo(r.get(4).ratePercent()));
        assertEquals(0, new BigDecimal("9.3").compareTo(r.get(5).ratePercent()));
    }

    @Test
    void motorStartsAtNinePercent() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOTOR, 2, "100000000", "35000000"));
        assertEquals(0, new BigDecimal("9").compareTo(r.get(0).ratePercent()));
        assertEquals(0, new BigDecimal("9.1").compareTo(r.get(1).ratePercent()));
    }

    @Test
    void singleYearInstallmentIsPrincipalPlusInterestOverTwelveMonths() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 1, "100000000", "35000000"));
        assertEquals(1, r.size());
        assertEquals(new BigDecimal("5850000.00"), r.get(0).monthlyAmount());
    }

    @Test
    void firstYearInstallmentForSixYearMobil() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 6, "1000000000", "500000000"));
        assertEquals(new BigDecimal("7500000.00"), r.get(0).monthlyAmount());
    }

    @Test
    void threeYearExampleMatchesReferenceNumbers() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 3, "100000000", "25000000"));
        assertEquals(new BigDecimal("2250000.00"), r.get(0).monthlyAmount());
        assertEquals(new BigDecimal("2432250.00"), r.get(1).monthlyAmount());
        assertEquals(new BigDecimal("2641423.50"), r.get(2).monthlyAmount());
    }

    @Test
    void roundingToTwoDecimalsDoesNotDriftTheNextYear() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 6, "1000000000", "500000000"));
        assertEquals(new BigDecimal("9570757.82"), r.get(3).monthlyAmount());
        assertEquals(new BigDecimal("10451267.53"), r.get(4).monthlyAmount());
        assertEquals(new BigDecimal("11423235.41"), r.get(5).monthlyAmount());
    }
}