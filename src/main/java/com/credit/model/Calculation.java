package com.credit.model;

import java.util.List;

public record Calculation(LoanRequest request, List<YearlyInstallment> installments) {
}
