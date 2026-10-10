package com.credit.factories;

import com.credit.constant.ErrorMessage;
import com.credit.exception.InvalidLoanException;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import com.credit.validator.LoanValidator;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.regex.Pattern;

public class LoanRequestFactory {

    private static final Pattern ALPHABET = Pattern.compile("^[A-Za-z]+$");
    private static final Pattern DIGITS = Pattern.compile("^\\d+$");
    private static final Pattern YEAR = Pattern.compile("^\\d{4}$");
    private static final int MAX_INT_DIGITS = 9;

    private final LoanValidator validator;

    public LoanRequestFactory() {
        this(Clock.systemDefaultZone());
    }

    public LoanRequestFactory(Clock clock) {
        this(new LoanValidator(clock));
    }

    public LoanRequestFactory(LoanValidator validator) {
        this.validator = validator;
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
        String value = requireAlphabet(raw, ErrorMessage.FIELD_VEHICLE_TYPE);
        for (VehicleType type : VehicleType.values()) {
            if (type.label().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new InvalidLoanException(ErrorMessage.VEHICLE_TYPE_INVALID);
    }

    public VehicleCondition parseCondition(String raw) {
        String value = requireAlphabet(raw, ErrorMessage.FIELD_CONDITION);
        for (VehicleCondition condition : VehicleCondition.values()) {
            if (condition.label().equalsIgnoreCase(value)) {
                return condition;
            }
        }
        throw new InvalidLoanException(ErrorMessage.CONDITION_INVALID);
    }

    public int parseYear(String raw, VehicleCondition condition) {
        String value = requireText(raw, ErrorMessage.FIELD_YEAR);
        if (!YEAR.matcher(value).matches()) {
            throw new InvalidLoanException(ErrorMessage.YEAR_FORMAT);
        }
        int year = Integer.parseInt(value);
        validator.validateYear(year, condition);
        return year;
    }

    public BigDecimal parseTotalLoan(String raw) {
        BigDecimal total = parseAmount(raw, ErrorMessage.FIELD_LOAN);
        validator.validateTotalLoan(total);
        return total;
    }

    public int parseTenor(String raw) {
        String value = requireText(raw, ErrorMessage.FIELD_TENOR);
        if (!DIGITS.matcher(value).matches() || value.length() > MAX_INT_DIGITS) {
            throw new InvalidLoanException(ErrorMessage.TENOR_FORMAT);
        }
        int tenor = Integer.parseInt(value);
        validator.validateTenor(tenor);
        return tenor;
    }

    public BigDecimal parseDownPayment(String raw, BigDecimal totalLoan, VehicleCondition condition) {
        BigDecimal downPayment = parseAmount(raw, ErrorMessage.FIELD_DOWN_PAYMENT);
        validator.validateDownPayment(downPayment, totalLoan, condition);
        return downPayment;
    }

    private String requireText(String raw, String field) {
        if (raw == null || raw.isBlank()) {
            throw new InvalidLoanException(ErrorMessage.REQUIRED.formatted(field));
        }
        return raw.trim();
    }

    private String requireAlphabet(String raw, String field) {
        String value = requireText(raw, field);
        if (!ALPHABET.matcher(value).matches()) {
            throw new InvalidLoanException(ErrorMessage.ALPHABET_ONLY.formatted(field));
        }
        return value;
    }

    private BigDecimal parseAmount(String raw, String field) {
        String value = requireText(raw, field);
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException e) {
            throw new InvalidLoanException(ErrorMessage.NUMBER_ONLY.formatted(field), e);
        }
    }
}
