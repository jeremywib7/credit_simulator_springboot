package com.credit.command;

import com.credit.service.LoanDataProvider;

public class LoadCommand implements Command {

    private final SessionContext session;
    private final LoanDataProvider provider;

    public LoadCommand(SessionContext session, LoanDataProvider provider) {
        this.session = session;
        this.provider = provider;
    }

    @Override
    public String name() {
        return "load";
    }

    @Override
    public String description() {
        return "Load data existing dari web service dan hitung otomatis";
    }

    @Override
    public void execute(String argument) {
        session.calculateAndShow(provider.fetch());
    }
}
