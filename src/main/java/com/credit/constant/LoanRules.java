package com.credit.constant;

import java.math.BigDecimal;

public final class LoanRules {

    public static final BigDecimal MAX_LOAN = new BigDecimal("1000000000");
    public static final int MIN_TENOR = 1;
    public static final int MAX_TENOR = 6;
    public static final int MONTHS_PER_YEAR = 12;
    public static final int INPUT_FILE_LINES = 6;
    public static final BigDecimal HUNDRED = new BigDecimal("100");
    public static final BigDecimal EVEN_YEAR_RATE_STEP = new BigDecimal("0.1");
    public static final BigDecimal ODD_YEAR_RATE_STEP = new BigDecimal("0.5");

    private LoanRules() {
    }
}
