package com.credit.command;

public class ExitCommand implements Command {

    private final SessionContext session;

    public ExitCommand(SessionContext session) {
        this.session = session;
    }

    @Override
    public String name() {
        return "exit";
    }

    @Override
    public String description() {
        return "Keluar dari aplikasi";
    }

    @Override
    public void execute(String argument) {
        session.stop();
    }
}
