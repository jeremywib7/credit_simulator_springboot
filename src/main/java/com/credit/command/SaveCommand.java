package com.credit.command;

import com.credit.constant.ErrorMessage;
import com.credit.exception.InvalidLoanException;
import com.credit.service.SheetManager;

public class SaveCommand implements Command {

    private final SessionContext session;
    private final SheetManager sheets;

    public SaveCommand(SessionContext session, SheetManager sheets) {
        this.session = session;
        this.sheets = sheets;
    }

    @Override
    public String name() {
        return "save";
    }

    @Override
    public String description() {
        return "Simpan hasil hitung terakhir ke sheet aktif";
    }

    @Override
    public void execute(String argument) {
        if (session.lastCalculation() == null) {
            throw new InvalidLoanException(ErrorMessage.NOTHING_TO_SAVE);
        }
        sheets.save(session.lastCalculation());
        session.view().println("Tersimpan di sheet '" + sheets.activeSheet() + "'.");
    }
}
