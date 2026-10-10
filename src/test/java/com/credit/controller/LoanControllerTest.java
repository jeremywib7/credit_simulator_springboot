package com.credit.controller;

import com.credit.exception.InvalidLoanException;
import com.credit.factory.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import com.credit.service.LoanApiClient;
import com.credit.service.LoanCalculator;
import com.credit.service.SheetManager;
import com.credit.view.ConsoleView;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoanControllerTest {

    private final LoanRequestFactory factory =
            new LoanRequestFactory(Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC));
    private final ByteArrayOutputStream captured = new ByteArrayOutputStream();
    private final SheetManager sheets = new SheetManager();

    private LoanController controller(String input, LoanApiClient api) {
        ConsoleView view = new ConsoleView(new BufferedReader(new StringReader(input)), new PrintStream(captured, true));
        return new LoanController(view, factory, new LoanCalculator(), api, sheets);
    }

    private LoanApiClient unusedApi() {
        return new LoanApiClient(HttpClient.newHttpClient(), factory, "http://127.0.0.1:1/none");
    }

    private LoanApiClient stubApi(LoanRequest request) {
        return new LoanApiClient(HttpClient.newHttpClient(), factory, "http://127.0.0.1:1/none") {
            @Override
            public LoanRequest fetch() {
                return request;
            }
        };
    }

    private String output() {
        return captured.toString();
    }

    @Test
    void showListsEveryRegisteredCommand() {
        LoanController c = controller("", unusedApi());
        c.dispatch("show");
        for (String name : c.commandNames()) {
            assertTrue(output().contains(name), "missing command: " + name);
        }
    }

    @Test
    void unknownCommandReportsError() {
        controller("", unusedApi()).dispatch("bogus");
        assertTrue(output().contains("tidak dikenal"));
    }

    @Test
    void blankLineIsIgnored() {
        controller("", unusedApi()).dispatch("   ");
        assertEquals("", output());
    }

    @Test
    void inputFlowPrintsInstallments() {
        String input = "input\nmobil\nbaru\n2025\n1000000000\n2\n500000000\nexit\n";
        controller(input, unusedApi()).runInteractive();
        assertTrue(output().contains("tahun 1 : Rp."));
        assertTrue(output().contains("Suku Bunga : 8%"));
        assertTrue(output().contains("Suku Bunga : 8,1%"));
    }

    @Test
    void invalidFieldIsAskedAgainWithoutRestartingTheFlow() {
        String input = "input\ntruk\nmobil\nbaru\n2030\n2025\n1000000000\n9\n2\n10\n500000000\nexit\n";
        controller(input, unusedApi()).runInteractive();
        assertTrue(output().contains("Error: Jenis kendaraan harus Mobil atau Motor."));
        assertTrue(output().contains("Error: Tahun kendaraan tidak boleh lebih dari 2025."));
        assertTrue(output().contains("Error: Tenor harus 1-6 tahun."));
        assertTrue(output().contains("Error: DP kendaraan Baru minimal 35%"));
        assertTrue(output().contains("tahun 2 : Rp."));
    }

    @Test
    void cancelKeywordAbortsInputAndReturnsToCommandPrompt() {
        String input = "input\nmobil\nBATAL\nshow\nexit\n";
        controller(input, unusedApi()).runInteractive();
        assertTrue(output().contains("Input dibatalkan."));
        assertTrue(output().contains("Command yang tersedia"));
    }

    @Test
    void endOfInputInsideInputFlowStopsCleanly() {
        controller("input\nmobil\n", unusedApi()).runInteractive();
        assertTrue(output().contains("Kondisi (Bekas|Baru)"));
    }

    @Test
    void loadCalculatesFromApiAutomatically() {
        LoanRequest req = new LoanRequest(VehicleType.MOBIL, VehicleCondition.BARU, 2025,
                new BigDecimal("1000000000"), 3, new BigDecimal("500000000"));
        controller("", stubApi(req)).dispatch("load");
        assertTrue(output().contains("tahun 3 : Rp."));
    }

    @Test
    void saveWithoutCalculationFails() {
        controller("", unusedApi()).dispatch("save");
        assertTrue(output().contains("Belum ada hasil hitung"));
    }

    @Test
    void saveAndSwitchSheetsKeepResultsSeparate() {
        LoanRequest req = new LoanRequest(VehicleType.MOTOR, VehicleCondition.BEKAS, 2010,
                new BigDecimal("50000000"), 1, new BigDecimal("20000000"));
        LoanController c = controller("", stubApi(req));
        c.dispatch("load");
        c.dispatch("save");
        assertEquals(1, sheets.calculations().size());
        c.dispatch("switch sheet2");
        assertEquals("sheet2", sheets.activeSheet());
        assertEquals(0, sheets.calculations().size());
        c.dispatch("list");
        assertTrue(output().contains("masih kosong"));
        c.dispatch("switch default");
        c.dispatch("list");
        assertTrue(output().contains("#1"));
    }

    @Test
    void switchWithoutNameFails() {
        controller("", unusedApi()).dispatch("switch");
        assertTrue(output().contains("Nama sheet wajib diisi"));
    }

    @Test
    void endOfInputStopsLoopWithoutError() {
        controller("show\n", unusedApi()).runInteractive();
        assertTrue(output().contains("Command yang tersedia"));
    }

    @Test
    void runFileCalculatesFromFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("in.txt");
        Files.writeString(file, "Mobil\nBaru\n2025\n1000000000\n6\n500000000\n");
        controller("", unusedApi()).runFile(file);
        assertTrue(output().contains("tahun 6 : Rp."));
    }

    @Test
    void runFileRejectsShortFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("in.txt");
        Files.writeString(file, "Mobil\nBaru\n");
        LoanController c = controller("", unusedApi());
        assertThrows(InvalidLoanException.class, () -> c.runFile(file));
    }

    @Test
    void runFileRejectsMissingFile(@TempDir Path dir) {
        LoanController c = controller("", unusedApi());
        assertThrows(InvalidLoanException.class, () -> c.runFile(dir.resolve("nope.txt")));
    }

    @Test
    void runFileRejectsInvalidRule(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("in.txt");
        Files.writeString(file, "Mobil\nBaru\n2025\n1000000000\n6\n100\n");
        LoanController c = controller("", unusedApi());
        assertThrows(InvalidLoanException.class, () -> c.runFile(file));
    }
}
