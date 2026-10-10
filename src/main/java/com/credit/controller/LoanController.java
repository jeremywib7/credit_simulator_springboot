package com.credit.controller;

import com.credit.command.CommandRegistry;
import com.credit.command.ExitCommand;
import com.credit.command.InputCommand;
import com.credit.command.ListCommand;
import com.credit.command.LoadCommand;
import com.credit.command.SaveCommand;
import com.credit.command.SessionContext;
import com.credit.command.ShowCommand;
import com.credit.command.SwitchCommand;
import com.credit.constant.ErrorMessage;
import com.credit.constant.LoanRules;
import com.credit.exception.InputCancelledException;
import com.credit.exception.InputEndedException;
import com.credit.exception.InvalidLoanException;
import com.credit.exception.LoanApiException;
import com.credit.factories.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.credit.service.LoanCalculator;
import com.credit.service.LoanDataProvider;
import com.credit.service.SheetManager;
import com.credit.view.ConsoleView;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoanController {

    private final ConsoleView view;
    private final LoanRequestFactory factory;
    private final SheetManager sheets;
    private final SessionContext session;
    private final CommandRegistry registry = new CommandRegistry();

    public LoanController(ConsoleView view, LoanRequestFactory factory, LoanCalculator calculator,
                          LoanDataProvider provider, SheetManager sheets) {
        this.view = view;
        this.factory = factory;
        this.sheets = sheets;
        this.session = new SessionContext(view, calculator);
        registry.register(new InputCommand(session, factory))
                .register(new LoadCommand(session, provider))
                .register(new ShowCommand(session, registry))
                .register(new SaveCommand(session, sheets))
                .register(new SwitchCommand(session, sheets))
                .register(new ListCommand(session, sheets))
                .register(new ExitCommand(session));
    }

    public List<String> commandNames() {
        return registry.names();
    }

    public void runInteractive() {
        view.println("=== CREDIT SIMULATOR === (ketik 'show' untuk melihat command)");
        try {
            while (session.isRunning()) {
                dispatch(view.prompt("[" + sheets.activeSheet() + "] command"));
            }
        } catch (InputEndedException e) {
            view.println("");
        }
    }

    public void dispatch(String line) {
        String trimmed = line == null ? "" : line.trim();
        if (trimmed.isEmpty()) {
            return;
        }
        String[] parts = trimmed.split("\\s+", 2);
        String argument = parts.length > 1 ? parts[1] : "";
        try {
            registry.find(parts[0]).ifPresentOrElse(
                    command -> command.execute(argument),
                    () -> view.showError(ErrorMessage.UNKNOWN_COMMAND.formatted(parts[0])));
        } catch (InvalidLoanException | LoanApiException e) {
            view.showError(e.getMessage());
        } catch (InputCancelledException e) {
            view.println(e.getMessage());
        }
    }

    public void runFile(Path file) {
        List<String> lines;
        try {
            lines = Files.readAllLines(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new InvalidLoanException(ErrorMessage.FILE_UNREADABLE.formatted(file, e.getMessage()), e);
        }
        if (lines.size() < LoanRules.INPUT_FILE_LINES) {
            throw new InvalidLoanException(ErrorMessage.FILE_INCOMPLETE);
        }
        LoanRequest request = factory.create(lines.get(0), lines.get(1), lines.get(2),
                lines.get(3), lines.get(4), lines.get(5));
        session.calculateAndShow(request);
    }
}
