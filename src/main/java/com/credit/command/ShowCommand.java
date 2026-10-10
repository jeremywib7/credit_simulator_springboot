package com.credit.command;

public class ShowCommand implements Command {

    private final SessionContext session;
    private final CommandRegistry registry;

    public ShowCommand(SessionContext session, CommandRegistry registry) {
        this.session = session;
        this.registry = registry;
    }

    @Override
    public String name() {
        return "show";
    }

    @Override
    public String description() {
        return "Tampilkan semua command yang tersedia";
    }

    @Override
    public void execute(String argument) {
        session.view().println("Command yang tersedia:");
        for (Command command : registry.all()) {
            session.view().println(String.format("  %-8s %s", command.name(), command.description()));
        }
    }
}
