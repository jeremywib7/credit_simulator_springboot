package com.credit.controller;

import com.credit.exception.InputCancelledException;
import com.credit.exception.InputEndedException;
import com.credit.exception.InvalidLoanException;
import com.credit.exception.LoanApiException;
import com.credit.factory.LoanRequestFactory;
import com.credit.model.Calculation;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import com.credit.service.LoanApiClient;
import com.credit.service.LoanCalculator;
import com.credit.service.SheetManager;
import com.credit.view.ConsoleView;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LoanController {

    private static final int FILE_FIELD_COUNT = 6;
    private static final String CANCEL_KEYWORD = "batal";

    private final ConsoleView view;
    private final LoanRequestFactory factory;
    private final LoanCalculator calculator;
    private final LoanApiClient apiClient;
    private final SheetManager sheets;
    private final Map<String, Command> commands = new LinkedHashMap<>();

    private Calculation last;
    private boolean running = true;

    public LoanController(ConsoleView view, LoanRequestFactory factory, LoanCalculator calculator,
                          LoanApiClient apiClient, SheetManager sheets) {
        this.view = view;
        this.factory = factory;
        this.calculator = calculator;
        this.apiClient = apiClient;
        this.sheets = sheets;
        registerCommands();
    }

    private void registerCommands() {
        register("input", "Input data kendaraan dan hitung cicilan", arg -> input());
        register("load", "Load data existing dari web service dan hitung otomatis", arg -> load());
        register("show", "Tampilkan semua command yang tersedia", arg -> showCommands());
        register("save", "Simpan hasil hitung terakhir ke sheet aktif", arg -> save());
        register("switch", "Pindah sheet: switch <nama> (dibuat jika belum ada)", this::switchSheet);
        register("list", "Tampilkan hasil tersimpan di sheet aktif", arg -> list());
        register("exit", "Keluar dari aplikasi", arg -> running = false);
    }

    private void register(String name, String description, CommandAction action) {
        commands.put(name, new Command(name, description, action));
    }

    public List<String> commandNames() {
        return List.copyOf(commands.keySet());
    }

    public void runInteractive() {
        view.println("=== CREDIT SIMULATOR === (ketik 'show' untuk melihat command)");
        try {
            while (running) {
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
        Command command = commands.get(parts[0].toLowerCase());
        if (command == null) {
            view.showError("Command '" + parts[0] + "' tidak dikenal. Ketik 'show'.");
            return;
        }
        try {
            command.action().run(parts.length > 1 ? parts[1] : "");
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
            throw new InvalidLoanException("Gagal membaca file '" + file + "': " + e.getMessage(), e);
        }
        if (lines.size() < FILE_FIELD_COUNT) {
            throw new InvalidLoanException("File harus berisi " + FILE_FIELD_COUNT
                    + " baris: jenis, kondisi, tahun, pinjaman, tenor, DP.");
        }
        LoanRequest request = factory.create(lines.get(0), lines.get(1), lines.get(2),
                lines.get(3), lines.get(4), lines.get(5));
        calculateAndShow(request);
    }

    private void input() {
        view.println("Ketik '" + CANCEL_KEYWORD + "' pada pertanyaan mana pun untuk membatalkan.");
        VehicleType type = ask("Jenis Kendaraan (Motor|Mobil)", factory::parseVehicleType);
        VehicleCondition condition = ask("Kondisi (Bekas|Baru)", factory::parseCondition);
        int year = ask("Tahun Kendaraan (4 digit)", raw -> factory.parseYear(raw, condition));
        BigDecimal total = ask("Jumlah Pinjaman Total (maks 1 miliar)", factory::parseTotalLoan);
        int tenor = ask("Tenor Pinjaman (1-6 tahun)", factory::parseTenor);
        BigDecimal downPayment = ask("Jumlah DP", raw -> factory.parseDownPayment(raw, total, condition));
        calculateAndShow(new LoanRequest(type, condition, year, total, tenor, downPayment));
    }

    private <T> T ask(String label, Function<String, T> parser) {
        while (true) {
            String raw = view.prompt(label);
            if (CANCEL_KEYWORD.equalsIgnoreCase(raw.trim())) {
                throw new InputCancelledException();
            }
            try {
                return parser.apply(raw);
            } catch (InvalidLoanException e) {
                view.showError(e.getMessage());
            }
        }
    }

    private void load() {
        calculateAndShow(apiClient.fetch());
    }

    private void calculateAndShow(LoanRequest request) {
        last = new Calculation(request, calculator.calculate(request));
        view.showCalculation(last);
    }

    private void showCommands() {
        view.println("Command yang tersedia:");
        for (Command command : commands.values()) {
            view.println(String.format("  %-8s %s", command.name(), command.description()));
        }
    }

    private void save() {
        if (last == null) {
            throw new InvalidLoanException("Belum ada hasil hitung untuk disimpan. Jalankan 'input' atau 'load'.");
        }
        sheets.save(last);
        view.println("Tersimpan di sheet '" + sheets.activeSheet() + "'.");
    }

    private void switchSheet(String name) {
        sheets.switchTo(name);
        view.println("Sheet aktif: '" + sheets.activeSheet() + "'. Sheet tersedia: " + sheets.sheetNames());
    }

    private void list() {
        List<Calculation> saved = sheets.calculations();
        if (saved.isEmpty()) {
            view.println("Sheet '" + sheets.activeSheet() + "' masih kosong.");
            return;
        }
        for (int i = 0; i < saved.size(); i++) {
            view.println("#" + (i + 1));
            view.showCalculation(saved.get(i));
        }
    }

    @FunctionalInterface
    private interface CommandAction {
        void run(String argument);
    }

    private record Command(String name, String description, CommandAction action) {
    }
}
