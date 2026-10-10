package com.credit.factories;

import com.credit.exception.InvalidLoanException;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanRequestFactoryTest {

    private final LoanRequestFactory factory =
            new LoanRequestFactory(Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC));

    @Test
    void createsRequestIgnoringCase() {
        LoanRequest r = factory.create("mObIL", "bARu", "2025", "1000000000", "6", "500000000");
        assertEquals(VehicleType.MOBIL, r.vehicleType());
        assertEquals(VehicleCondition.BARU, r.condition());
        assertEquals(6, r.tenorYears());
    }

    @Test
    void rejectsUnknownVehicleType() {
        assertThrows(InvalidLoanException.class, () -> factory.parseVehicleType("Truk"));
    }

    @Test
    void rejectsNonAlphabetVehicleType() {
        assertThrows(InvalidLoanException.class, () -> factory.parseVehicleType("Mobil1"));
    }

    @Test
    void rejectsBlankCondition() {
        assertThrows(InvalidLoanException.class, () -> factory.parseCondition(" "));
        assertThrows(InvalidLoanException.class, () -> factory.parseCondition(null));
    }

    @Test
    void rejectsUnknownCondition() {
        assertThrows(InvalidLoanException.class, () -> factory.parseCondition("Rusak"));
    }

    @Test
    void rejectsYearNotFourDigits() {
        assertThrows(InvalidLoanException.class, () -> factory.parseYear("25", VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class, () -> factory.parseYear("abcd", VehicleCondition.BEKAS));
    }

    @Test
    void newVehicleYearMustBeCurrentYearMinusOneOrNewer() {
        assertEquals(2024, factory.parseYear("2024", VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class, () -> factory.parseYear("2023", VehicleCondition.BARU));
    }

    @Test
    void yearInTheFutureIsRejectedForAnyCondition() {
        assertEquals(2025, factory.parseYear("2025", VehicleCondition.BARU));
        assertEquals(2025, factory.parseYear("2025", VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class, () -> factory.parseYear("2026", VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class, () -> factory.parseYear("9999", VehicleCondition.BEKAS));
    }

    @Test
    void usedVehicleAcceptsOldYear() {
        assertEquals(2010, factory.parseYear("2010", VehicleCondition.BEKAS));
    }

    @Test
    void totalLoanBoundaries() {
        assertEquals(new BigDecimal("1000000000"), factory.parseTotalLoan("1000000000"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTotalLoan("1000000001"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTotalLoan("0"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTotalLoan("-5"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTotalLoan("abc"));
    }

    @Test
    void tenorBoundaries() {
        assertEquals(1, factory.parseTenor("1"));
        assertEquals(6, factory.parseTenor("6"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTenor("0"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTenor("7"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTenor("x"));
        assertThrows(InvalidLoanException.class, () -> factory.parseTenor("99999999999"));
    }

    @Test
    void newVehicleDownPaymentMinimum35Percent() {
        BigDecimal total = new BigDecimal("100000000");
        assertEquals(new BigDecimal("35000000"),
                factory.parseDownPayment("35000000", total, VehicleCondition.BARU));
        assertThrows(InvalidLoanException.class,
                () -> factory.parseDownPayment("34999999", total, VehicleCondition.BARU));
    }

    @Test
    void usedVehicleDownPaymentMinimum25Percent() {
        BigDecimal total = new BigDecimal("100000000");
        assertEquals(new BigDecimal("25000000"),
                factory.parseDownPayment("25000000", total, VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class,
                () -> factory.parseDownPayment("24999999", total, VehicleCondition.BEKAS));
    }

    @Test
    void downPaymentMustBeBelowTotalAndNotNegative() {
        BigDecimal total = new BigDecimal("100000000");
        assertThrows(InvalidLoanException.class,
                () -> factory.parseDownPayment("100000000", total, VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class,
                () -> factory.parseDownPayment("-1", total, VehicleCondition.BEKAS));
        assertThrows(InvalidLoanException.class,
                () -> factory.parseDownPayment("abc", total, VehicleCondition.BEKAS));
    }
}
