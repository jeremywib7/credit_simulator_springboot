package com.credit.service;

import com.credit.model.LoanRequest;
import com.credit.model.YearlyInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculator {

    private static final BigDecimal EVEN_YEAR_STEP = new BigDecimal("0.1");
    private static final BigDecimal ODD_YEAR_STEP = new BigDecimal("0.5");
    private static final BigDecimal HUNDRED = new BigDecimal("100");
    private static final int MONTHS_PER_YEAR = 12;

    public List<YearlyInstallment> calculate(LoanRequest request) {
        BigDecimal balance = request.totalLoan().subtract(request.downPayment());
        BigDecimal rate = request.vehicleType().baseRatePercent();
        List<YearlyInstallment> result = new ArrayList<>();

        for (int year = 1; year <= request.tenorYears(); year++) {
            if (year > 1) {
                rate = rate.add(year % 2 == 0 ? EVEN_YEAR_STEP : ODD_YEAR_STEP);
            }
            BigDecimal withInterest = balance.add(balance.multiply(rate).divide(HUNDRED));
            int remainingMonths = (request.tenorYears() - year + 1) * MONTHS_PER_YEAR;
            BigDecimal monthly = withInterest.divide(BigDecimal.valueOf(remainingMonths), 10, RoundingMode.HALF_UP);
            balance = withInterest.subtract(monthly.multiply(BigDecimal.valueOf(MONTHS_PER_YEAR)));
            result.add(new YearlyInstallment(year, monthly.setScale(2, RoundingMode.HALF_UP), rate));
        }
        return result;
    }
}
