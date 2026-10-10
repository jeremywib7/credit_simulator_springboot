package com.credit.command;

import com.credit.exception.InputCancelledException;
import com.credit.exception.InvalidLoanException;
import com.credit.factories.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;

import java.math.BigDecimal;
import java.util.function.Function;

public class InputCommand implements Command {

    public static final String CANCEL_KEYWORD = "batal";

    private final SessionContext session;
    private final LoanRequestFactory factory;

    public InputCommand(SessionContext session, LoanRequestFactory factory) {
        this.session = session;
        this.factory = factory;
    }

    @Override
    public String name() {
        return "input";
    }

    @Override
    public String description() {
        return "Input data kendaraan dan hitung cicilan";
    }

    @Override
    public void execute(String argument) {
        session.view().println("Ketik '" + CANCEL_KEYWORD + "' pada pertanyaan mana pun untuk membatalkan.");
        VehicleType type = ask("Jenis Kendaraan (Motor|Mobil)", factory::parseVehicleType);
        VehicleCondition condition = ask("Kondisi (Bekas|Baru)", factory::parseCondition);
        int year = ask("Tahun Kendaraan (4 digit)", raw -> factory.parseYear(raw, condition));
        BigDecimal total = ask("Jumlah Pinjaman Total (maks 1 miliar)", factory::parseTotalLoan);
        int tenor = ask("Tenor Pinjaman (1-6 tahun)", factory::parseTenor);
        BigDecimal downPayment = ask("Jumlah DP", raw -> factory.parseDownPayment(raw, total, condition));
        session.calculateAndShow(new LoanRequest(type, condition, year, total, tenor, downPayment));
    }

    private <T> T ask(String label, Function<String, T> parser) {
        while (true) {
            String raw = session.view().prompt(label);
            if (CANCEL_KEYWORD.equalsIgnoreCase(raw.trim())) {
                throw new InputCancelledException();
            }
            try {
                return parser.apply(raw);
            } catch (InvalidLoanException e) {
                session.view().showError(e.getMessage());
            }
        }
    }
}
