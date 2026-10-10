package com.credit.service;

import com.credit.exception.InvalidLoanException;
import com.credit.model.Calculation;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SheetManagerTest {

    private static Calculation calculation() {
        LoanRequest request = new LoanRequest(VehicleType.MOBIL, VehicleCondition.BEKAS, 2020,
                new BigDecimal("100000000"), 1, new BigDecimal("25000000"));
        return new Calculation(request, List.of());
    }

    @Test
    void startsOnDefaultSheetWithNoResults() {
        SheetManager sheets = new SheetManager();
        assertEquals(SheetManager.DEFAULT_SHEET, sheets.activeSheet());
        assertTrue(sheets.calculations().isEmpty());
    }

    @Test
    void switchRejectsNullAndBlankNames() {
        SheetManager sheets = new SheetManager();
        assertThrows(InvalidLoanException.class, () -> sheets.switchTo(null));
        assertThrows(InvalidLoanException.class, () -> sheets.switchTo("   "));
        assertEquals(SheetManager.DEFAULT_SHEET, sheets.activeSheet());
    }

    @Test
    void sheetsKeepTheirOwnResultsAndNamesAreTrimmed() {
        SheetManager sheets = new SheetManager();
        sheets.save(calculation());
        sheets.switchTo("  other ");
        assertEquals("other", sheets.activeSheet());
        assertTrue(sheets.calculations().isEmpty());
        sheets.switchTo(SheetManager.DEFAULT_SHEET);
        assertEquals(1, sheets.calculations().size());
        assertEquals(List.of(SheetManager.DEFAULT_SHEET, "other"), sheets.sheetNames());
    }
}
