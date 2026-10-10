package com.credit.validator;

import com.credit.exception.InvalidLoanException;
import com.credit.model.VehicleCondition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanValidatorTest {

    private static final BigDecimal LOAN = new BigDecimal("100000000");

    private final LoanValidator validator =
            new LoanValidator(Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void yearBoundaries() {
        assertDoesNotThrow(() -> validator.validateYear(2025, VehicleCondition.BARU));
        assertDoesNotThrow(() -> validator.validateYear(2024, VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class, () -> validator.validateYear(2023, VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class, () -> validator.validateYear(2026, VehicleCondition.BEKAS));
        assertDoesNotThrow(() -> validator.validateYear(1990, VehicleCondition.BEKAS));
    }

    @Test
    void totalLoanBoundaries() {
        assertDoesNotThrow(() -> validator.validateTotalLoan(new BigDecimal("1000000000")));
        assertThrows(InvalidLoanException.class, () -> validator.validateTotalLoan(new BigDecimal("1000000001")));
        assertThrows(InvalidLoanException.class, () -> validator.validateTotalLoan(BigDecimal.ZERO));
    }

    @Test
    void tenorBoundaries() {
        assertDoesNotThrow(() -> validator.validateTenor(1));
        assertDoesNotThrow(() -> validator.validateTenor(6));
        assertThrows(InvalidLoanException.class, () -> validator.validateTenor(0));
        assertThrows(InvalidLoanException.class, () -> validator.validateTenor(7));
    }

    @Test
    void downPaymentMinimumsPerCondition() {
        assertDoesNotThrow(() -> validator.validateDownPayment(new BigDecimal("35000000"), LOAN, VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class,
                () -> validator.validateDownPayment(new BigDecimal("34999999"), LOAN, VehicleCondition.BARU));
        assertDoesNotThrow(() -> validator.validateDownPayment(new BigDecimal("25000000"), LOAN, VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class,
                () -> validator.validateDownPayment(new BigDecimal("24999999"), LOAN, VehicleCondition.BEKAS));
    }

    @Test
    void downPaymentMustBeNonNegativeAndBelowLoan() {
        assertThrows(InvalidLoanException.class,
                () -> validator.validateDownPayment(new BigDecimal("-1"), LOAN, VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class,
                () -> validator.validateDownPayment(LOAN, LOAN, VehicleCondition.BEKAS));
    }
}
