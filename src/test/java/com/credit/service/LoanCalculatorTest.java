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
    void lastYearClearsRemainingBalance() {
        List<YearlyInstallment> r = calculator.calculate(request(VehicleType.MOBIL, 2, "200000000", "70000000"));
        BigDecimal yearOneTotal = new BigDecimal("130000000").multiply(new BigDecimal("1.08"));
        BigDecimal paidYearOne = r.get(0).monthlyAmount().multiply(BigDecimal.valueOf(12));
        BigDecimal balance = yearOneTotal.subtract(paidYearOne);
        BigDecimal dueYearTwo = balance.multiply(new BigDecimal("1.081"));
        BigDecimal paidYearTwo = r.get(1).monthlyAmount().multiply(BigDecimal.valueOf(12));
        assertEquals(0, dueYearTwo.subtract(paidYearTwo).abs().compareTo(new BigDecimal("1")) < 0 ? 0 : 1);
    }
}
