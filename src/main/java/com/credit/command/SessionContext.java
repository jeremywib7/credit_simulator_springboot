package com.credit.command;

import com.credit.model.Calculation;
import com.credit.model.LoanRequest;
import com.credit.service.LoanCalculator;
import com.credit.view.ConsoleView;

public class SessionContext {

    private final ConsoleView view;
    private final LoanCalculator calculator;
    private Calculation last;
    private boolean running = true;

    public SessionContext(ConsoleView view, LoanCalculator calculator) {
        this.view = view;
        this.calculator = calculator;
    }

    public ConsoleView view() {
        return view;
    }

    public Calculation lastCalculation() {
        return last;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public void calculateAndShow(LoanRequest request) {
        last = new Calculation(request, calculator.calculate(request));
        view.showCalculation(last);
    }
}
