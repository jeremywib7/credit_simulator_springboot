package com.credit.command;

import com.credit.service.SheetManager;

public class SwitchCommand implements Command {

    private final SessionContext session;
    private final SheetManager sheets;

    public SwitchCommand(SessionContext session, SheetManager sheets) {
        this.session = session;
        this.sheets = sheets;
    }

    @Override
    public String name() {
        return "switch";
    }

    @Override
    public String description() {
        return "Pindah sheet: switch <nama> (dibuat jika belum ada)";
    }

    @Override
    public void execute(String argument) {
        sheets.switchTo(argument);
        session.view().println("Sheet aktif: '" + sheets.activeSheet() + "'. Sheet tersedia: " + sheets.sheetNames());
    }
}
