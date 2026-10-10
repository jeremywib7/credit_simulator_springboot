package com.credit;

import com.credit.controller.LoanController;
import com.credit.exception.InvalidLoanException;
import com.credit.factories.LoanRequestFactory;
import com.credit.service.LoanApiClient;
import com.credit.service.LoanCalculator;
import com.credit.service.SheetManager;
import com.credit.view.ConsoleView;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class CreditSimulator {

    private CreditSimulator() {
    }

    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        ConsoleView view = new ConsoleView(in, System.out);
        LoanRequestFactory factory = new LoanRequestFactory();
        LoanController controller = new LoanController(view, factory, new LoanCalculator(),
                LoanApiClient.createDefault(factory), new SheetManager());

        if (args.length == 0) {
            controller.runInteractive();
            return;
        }
        try {
            controller.runFile(Path.of(args[0]));
        } catch (InvalidLoanException e) {
            view.showError(e.getMessage());
            System.exit(1);
        }
    }
}
