package com.credit.service;

import com.credit.constant.ErrorMessage;
import com.credit.exception.InvalidLoanException;
import com.credit.model.Calculation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SheetManager {

    public static final String DEFAULT_SHEET = "default";

    private final Map<String, List<Calculation>> sheets = new LinkedHashMap<>();
    private String active = DEFAULT_SHEET;

    public SheetManager() {
        sheets.put(DEFAULT_SHEET, new ArrayList<>());
    }

    public String activeSheet() {
        return active;
    }

    public void save(Calculation calculation) {
        sheets.get(active).add(calculation);
    }

    public void switchTo(String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidLoanException(ErrorMessage.SHEET_NAME_REQUIRED);
        }
        active = name.trim();
        sheets.computeIfAbsent(active, key -> new ArrayList<>());
    }

    public List<Calculation> calculations() {
        return Collections.unmodifiableList(sheets.get(active));
    }

    public List<String> sheetNames() {
        return List.copyOf(sheets.keySet());
    }
}
