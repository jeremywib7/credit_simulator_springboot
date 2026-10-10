package com.credit.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public final class AppConfig {

    public static final String ENV_API_URL = "CREDIT_API_URL";
    public static final String API_URL_KEY = "api.url";
    public static final String LOCAL_FILE = "credit_simulator.properties";

    private static final String CLASSPATH_FILE = "/application.properties";

    private AppConfig() {
    }

    public static String apiUrl() {
        return apiUrl(System.getenv(), Path.of(LOCAL_FILE));
    }

    static String apiUrl(Map<String, String> env, Path localFile) {
        String fromEnv = env.get(ENV_API_URL);
        if (fromEnv != null && !fromEnv.isBlank()) {
            return fromEnv.trim();
        }
        return readLocal(localFile)
                .or(AppConfig::readBundled)
                .orElseThrow(() -> new IllegalStateException(
                        "Property '" + API_URL_KEY + "' tidak ditemukan di " + LOCAL_FILE + " atau application.properties."));
    }

    private static Optional<String> readLocal(Path file) {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(file)) {
            return apiUrlFrom(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca " + file + ": " + e.getMessage(), e);
        }
    }

    private static Optional<String> readBundled() {
        try (InputStream in = AppConfig.class.getResourceAsStream(CLASSPATH_FILE)) {
            return in == null ? Optional.empty() : apiUrlFrom(in);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca application.properties: " + e.getMessage(), e);
        }
    }

    private static Optional<String> apiUrlFrom(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);
        String value = properties.getProperty(API_URL_KEY);
        return value == null || value.isBlank() ? Optional.empty() : Optional.of(value.trim());
    }
}
