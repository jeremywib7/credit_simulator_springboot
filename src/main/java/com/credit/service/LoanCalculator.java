package com.credit.service;

import com.credit.constant.LoanRules;
import com.credit.model.LoanRequest;
import com.credit.model.YearlyInstallment;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public class LoanCalculator {

    public List<YearlyInstallment> calculate(LoanRequest request) {
        BigDecimal balance = request.totalLoan().subtract(request.downPayment());
        BigDecimal rate = request.vehicleType().baseRatePercent();
        List<YearlyInstallment> result = new ArrayList<>();

        for (int year = 1; year <= request.tenorYears(); year++) {
            if (year > 1) {
                rate = rate.add(year % 2 == 0 ? LoanRules.EVEN_YEAR_RATE_STEP : LoanRules.ODD_YEAR_RATE_STEP);
            }
            BigDecimal withInterest = balance.add(balance.multiply(rate).divide(LoanRules.HUNDRED));
            int remainingMonths = (request.tenorYears() - year + 1) * LoanRules.MONTHS_PER_YEAR;
            BigDecimal monthly = withInterest.divide(BigDecimal.valueOf(remainingMonths), 10, RoundingMode.HALF_UP);
            balance = withInterest.subtract(monthly.multiply(BigDecimal.valueOf(LoanRules.MONTHS_PER_YEAR)));
            result.add(new YearlyInstallment(year, monthly.setScale(2, RoundingMode.HALF_UP), rate));
        }
        return result;
    }
}
