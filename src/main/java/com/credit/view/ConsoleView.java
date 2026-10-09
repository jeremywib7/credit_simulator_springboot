package com.credit.view;

import com.credit.exception.InputEndedException;
import com.credit.model.Calculation;
import com.credit.model.LoanRequest;
import com.credit.model.YearlyInstallment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class ConsoleView {

    private final BufferedReader in;
    private final PrintStream out;

    public ConsoleView(BufferedReader in, PrintStream out) {
        this.in = in;
        this.out = out;
    }

    public String prompt(String label) {
        out.print(label + ": ");
        out.flush();
        try {
            String line = in.readLine();
            if (line == null) {
                throw new InputEndedException();
            }
            return line;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public void println(String message) {
        out.println(message);
    }

    public void showError(String message) {
        out.println("Error: " + message);
    }

    public void showCalculation(Calculation calculation) {
        LoanRequest r = calculation.request();
        out.println(r.vehicleType().label() + " " + r.condition().label() + " " + r.year()
                + " | Pinjaman " + money(r.totalLoan()) + " | DP " + money(r.downPayment())
                + " | Tenor " + r.tenorYears() + " thn");
        showInstallments(calculation.installments());
    }

    public void showInstallments(List<YearlyInstallment> installments) {
        for (YearlyInstallment item : installments) {
            out.println("tahun " + item.year() + " : Rp. " + money(item.monthlyAmount())
                    + "/bln , Suku Bunga : " + rate(item.ratePercent()) + "%");
        }
    }

    static String money(BigDecimal value) {
        DecimalFormat format = new DecimalFormat("#,##0.00", DecimalFormatSymbols.getInstance(Locale.US));
        return format.format(value);
    }

    static String rate(BigDecimal percent) {
        return percent.stripTrailingZeros().toPlainString().replace('.', ',');
    }
}
