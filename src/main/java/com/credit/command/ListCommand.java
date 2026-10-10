package com.credit.command;

import com.credit.model.Calculation;
import com.credit.service.SheetManager;

import java.util.List;

public class ListCommand implements Command {

    private final SessionContext session;
    private final SheetManager sheets;

    public ListCommand(SessionContext session, SheetManager sheets) {
        this.session = session;
        this.sheets = sheets;
    }

    @Override
    public String name() {
        return "list";
    }

    @Override
    public String description() {
        return "Tampilkan hasil tersimpan di sheet aktif";
    }

    @Override
    public void execute(String argument) {
        List<Calculation> saved = sheets.calculations();
        if (saved.isEmpty()) {
            session.view().println("Sheet '" + sheets.activeSheet() + "' masih kosong.");
            return;
        }
        for (int i = 0; i < saved.size(); i++) {
            session.view().println("#" + (i + 1));
            session.view().showCalculation(saved.get(i));
        }
    }
}
