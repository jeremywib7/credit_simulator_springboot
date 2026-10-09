package com.credit.factory;

import com.credit.exception.InvalidLoanException;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Year;
import java.util.regex.Pattern;

public class LoanRequestFactory {

    public static final BigDecimal MAX_LOAN = new BigDecimal("1000000000");
    public static final int MAX_TENOR = 6;

    private static final Pattern ALPHABET = Pattern.compile("^[A-Za-z]+$");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final Pattern YEAR = Pattern.compile("^\\d{4}$");
    private static final BigDecimal HUNDRED = new BigDecimal("100");

    private final Clock clock;

    public LoanRequestFactory() {
        this(Clock.systemDefaultZone());
    }

    public LoanRequestFactory(Clock clock) {
        this.clock = clock;
    }

    public LoanRequest create(String type, String condition, String year, String totalLoan,
                              String tenor, String downPayment) {
        VehicleType vehicleType = parseVehicleType(type);
        VehicleCondition vehicleCondition = parseCondition(condition);
        int vehicleYear = parseYear(year, vehicleCondition);
        BigDecimal total = parseTotalLoan(totalLoan);
        int tenorYears = parseTenor(tenor);
        BigDecimal dp = parseDownPayment(downPayment, total, vehicleCondition);
        return new LoanRequest(vehicleType, vehicleCondition, vehicleYear, total, tenorYears, dp);
    }

    public VehicleType parseVehicleType(String raw) {
        String value = requireAlphabet(raw, "Jenis kendaraan");
        for (VehicleType type : VehicleType.values()) {
            if (type.label().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new InvalidLoanException("Jenis kendaraan harus Mobil atau Motor.");
    }

    public VehicleCondition parseCondition(String raw) {
        String value = requireAlphabet(raw, "Kondisi kendaraan");
        for (VehicleCondition condition : VehicleCondition.values()) {
            if (condition.label().equalsIgnoreCase(value)) {
                return condition;
            }
        }
        throw new InvalidLoanException("Kondisi kendaraan harus Baru atau Bekas.");
    }

    public int parseYear(String raw, VehicleCondition condition) {
        String value = requireText(raw, "Tahun kendaraan");
        if (!YEAR.matcher(value).matches()) {
            throw new InvalidLoanException("Tahun kendaraan harus 4 digit angka.");
        }
        int year = Integer.parseInt(value);
        int minYear = Year.now(clock).getValue() - 1;
        if (condition == VehicleCondition.BARU && year < minYear) {
            throw new InvalidLoanException("Tahun kendaraan baru tidak boleh kurang dari " + minYear + ".");
        }
        return year;
    }

    public BigDecimal parseTotalLoan(String raw) {
        BigDecimal total = parseAmount(raw, "Jumlah pinjaman");
        if (total.signum() <= 0) {
            throw new InvalidLoanException("Jumlah pinjaman harus lebih dari 0.");
        }
        if (total.compareTo(MAX_LOAN) > 0) {
            throw new InvalidLoanException("Jumlah pinjaman maksimal 1 miliar.");
        }
        return total;
    }

    public int parseTenor(String raw) {
        String value = requireText(raw, "Tenor");
        if (!DIGITS.matcher(value).matches() || value.length() > 9) {
            throw new InvalidLoanException("Tenor harus berupa angka 1-" + MAX_TENOR + " tahun.");
        }
        int tenor = Integer.parseInt(value);
        if (tenor < 1 || tenor > MAX_TENOR) {
            throw new InvalidLoanException("Tenor harus 1-" + MAX_TENOR + " tahun.");
        }
        return tenor;
    }

    public BigDecimal parseDownPayment(String raw, BigDecimal totalLoan, VehicleCondition condition) {
        BigDecimal dp = parseAmount(raw, "Jumlah DP");
        if (dp.signum() < 0) {
            throw new InvalidLoanException("Jumlah DP tidak boleh negatif.");
        }
        if (dp.compareTo(totalLoan) >= 0) {
            throw new InvalidLoanException("Jumlah DP harus lebih kecil dari jumlah pinjaman.");
        }
        BigDecimal minDp = totalLoan.multiply(condition.minDownPaymentPercent()).divide(HUNDRED);
        if (dp.compareTo(minDp) < 0) {
            throw new InvalidLoanException("DP kendaraan " + condition.label() + " minimal "
                    + condition.minDownPaymentPercent().toPlainString() + "% dari jumlah pinjaman.");
        }
        return dp;
    }

    private String requireText(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidLoanException(field + " wajib diisi.");
        }
        return raw.trim();
    }

    private String requireAlphabet(String raw, String field) {
        String value = requireText(raw, field);
        if (!ALPHABET.matcher(value).matches()) {
            throw new InvalidLoanException(field + " hanya boleh huruf.");
        }
        return value;
    }

    private BigDecimal parseAmount(String raw, String field) {
        String value = requireText(raw, field);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new InvalidLoanException(field + " harus berupa angka.", e);
        }
    }
}
