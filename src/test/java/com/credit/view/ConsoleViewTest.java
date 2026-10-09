package com.credit.view;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ConsoleViewTest {

    @Test
    void moneyUsesUsGroupingWithTwoDecimals() {
        assertEquals("3,500,000.00", ConsoleView.money(new BigDecimal("3500000")));
    }

    @Test
    void rateUsesCommaAndDropsTrailingZeros() {
        assertEquals("8", ConsoleView.rate(new BigDecimal("8.0")));
        assertEquals("8,1", ConsoleView.rate(new BigDecimal("8.10")));
        assertEquals("8,6", ConsoleView.rate(new BigDecimal("8.6")));
    }
}
