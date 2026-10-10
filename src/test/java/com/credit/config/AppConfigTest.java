package com.credit.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AppConfigTest {

    private static final String MOCKY = "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";
    private static final String ENV = "CREDIT_API_URL";

    @Test
    void usesDefaultAfterColonWhenEnvIsNotSet(@TempDir Path dir) {
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), dir.resolve("missing.properties")));
    }

    @Test
    void environmentVariableOverridesBundledDefault(@TempDir Path dir) {
        assertEquals("http://from-env", AppConfig.apiUrl(Map.of(ENV, " http://from-env "), dir.resolve("none")));
    }

    @Test
    void blankEnvironmentVariableFallsBackToDefault(@TempDir Path dir) {
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(ENV, "  "), dir.resolve("none")));
    }

    @Test
    void localFileOverridesBundledDefault(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "api.url=http://127.0.0.1:8765/loan.json\n");
        assertEquals("http://127.0.0.1:8765/loan.json", AppConfig.apiUrl(Map.of(ENV, "http://ignored"), file));
    }

    @Test
    void localFileMayAlsoUsePlaceholders(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "api.url=${MOCK_HOST:http://localhost:1}/loan.json\n");
        assertEquals("http://mock/loan.json", AppConfig.apiUrl(Map.of("MOCK_HOST", "http://mock"), file));
        assertEquals("http://localhost:1/loan.json", AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void localFileWithoutKeyFallsBackToBundled(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "other=1\napi.url=  \n");
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void localFileWithoutApiUrlKeyFallsBackToBundled(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "other=1\n");
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void localPlaceholderResolvingToBlankFallsBackToBundled(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "api.url=${UNSET:}\n");
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void noArgumentOverloadAlwaysYieldsAUrl() {
        assertFalse(AppConfig.apiUrl().isBlank());
    }

    @Test
    void resolveKeepsLiteralTextAndEmptyDefault() {
        assertEquals("plain", AppConfig.resolve("plain", Map.of()));
        assertEquals("", AppConfig.resolve("${X:}", Map.of()));
        assertEquals("a-b-c", AppConfig.resolve("a-${X:b}-${Y:c}", Map.of()));
    }

    @Test
    void resolveRejectsMissingVariableWithoutDefault() {
        assertThrows(IllegalStateException.class, () -> AppConfig.resolve("${NOPE}", Map.of()));
    }

    @Test
    void replacementWithDollarOrBackslashIsTakenLiterally() {
        assertEquals("http://h/a$1\\b", AppConfig.resolve("${U}", Map.of("U", "http://h/a$1\\b")));
    }
}
