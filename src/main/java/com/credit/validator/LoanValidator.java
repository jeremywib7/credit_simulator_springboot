package com.credit.validator;

import com.credit.constant.ErrorMessage;
import com.credit.constant.LoanRules;
import com.credit.exception.InvalidLoanException;
import com.credit.model.VehicleCondition;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;

public class LoanValidator {

    private final Clock clock;

    public LoanValidator(Clock clock) {
        this.clock = clock;
    }

    public void validateYear(int year, VehicleCondition condition) {
        int currentYear = Year.now(clock).getValue();
        if (year > currentYear) {
            throw new InvalidLoanException(ErrorMessage.YEAR_IN_FUTURE.formatted(currentYear));
        }
        int minYear = currentYear - 1;
        if (condition == VehicleCondition.BARU && year < minYear) {
            throw new InvalidLoanException(ErrorMessage.YEAR_NEW_TOO_OLD.formatted(minYear));
        }
    }

    public void validateTotalLoan(BigDecimal totalLoan) {
        if (totalLoan.signum() <= 0) {
            throw new InvalidLoanException(ErrorMessage.LOAN_NOT_POSITIVE);
        }
        if (totalLoan.compareTo(LoanRules.MAX_LOAN) > 0) {
            throw new InvalidLoanException(ErrorMessage.LOAN_TOO_BIG);
        }
    }

    public void validateTenor(int tenorYears) {
        if (tenorYears < LoanRules.MIN_TENOR || tenorYears > LoanRules.MAX_TENOR) {
            throw new InvalidLoanException(ErrorMessage.TENOR_RANGE);
        }
    }

    public void validateDownPayment(BigDecimal downPayment, BigDecimal totalLoan, VehicleCondition condition) {
        if (downPayment.signum() < 0) {
            throw new InvalidLoanException(ErrorMessage.DP_NEGATIVE);
        }
        if (downPayment.compareTo(totalLoan) >= 0) {
            throw new InvalidLoanException(ErrorMessage.DP_NOT_BELOW_LOAN);
        }
        BigDecimal minimum = totalLoan.multiply(condition.minDownPaymentPercent()).divide(LoanRules.HUNDRED);
        if (downPayment.compareTo(minimum) < 0) {
            throw new InvalidLoanException(ErrorMessage.DP_BELOW_MINIMUM
                    .formatted(condition.label(), condition.minDownPaymentPercent().toPlainString()));
        }
    }
}
