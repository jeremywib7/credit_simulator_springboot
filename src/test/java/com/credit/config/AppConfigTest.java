package com.credit.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppConfigTest {

    private static final String MOCKY = "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";

    @Test
    void usesBundledPropertyWhenNothingElseIsSet(@TempDir Path dir) {
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), dir.resolve("missing.properties")));
    }

    @Test
    void localFileOverridesBundledProperty(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "api.url=http://127.0.0.1:8765/loan.json\n");
        assertEquals("http://127.0.0.1:8765/loan.json", AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void environmentVariableOverridesLocalFile(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "api.url=http://from-file\n");
        assertEquals("http://from-env", AppConfig.apiUrl(Map.of(AppConfig.ENV_API_URL, " http://from-env "), file));
    }

    @Test
    void blankEnvironmentVariableIsIgnored(@TempDir Path dir) {
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(AppConfig.ENV_API_URL, "  "), dir.resolve("none")));
    }

    @Test
    void localFileWithoutKeyFallsBackToBundled(@TempDir Path dir) throws IOException {
        Path file = dir.resolve("credit_simulator.properties");
        Files.writeString(file, "other=1\napi.url=  \n");
        assertEquals(MOCKY, AppConfig.apiUrl(Map.of(), file));
    }

    @Test
    void bundledPropertyIsAnHttpsUrl() {
        assertTrue(AppConfig.apiUrl(Map.of(), Path.of("does-not-exist")).startsWith("https://"));
    }
}
