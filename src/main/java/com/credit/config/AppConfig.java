package com.credit.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AppConfig {

    public static final String API_URL_KEY = "api.url";
    public static final String LOCAL_FILE = "credit_simulator.properties";

    private static final String CLASSPATH_FILE = "/application.properties";
    private static final Pattern PLACEHOLDER = Pattern.compile("\\$\\{([^:}]+)(?::([^}]*))?}");

    private AppConfig() {
    }

    public static String apiUrl() {
        return apiUrl(System.getenv(), Path.of(LOCAL_FILE));
    }

    static String apiUrl(Map<String, String> env, Path localFile) {
        return readLocal(localFile, env)
                .or(() -> readBundled(env))
                .orElseThrow(() -> new IllegalStateException(
                        "Property '" + API_URL_KEY + "' tidak ditemukan di " + LOCAL_FILE + " atau application.properties."));
    }

    static String resolve(String value, Map<String, String> env) {
        Matcher matcher = PLACEHOLDER.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String fromEnv = env.get(matcher.group(1));
            String replacement = fromEnv != null && !fromEnv.isBlank() ? fromEnv : matcher.group(2);
            if (replacement == null) {
                throw new IllegalStateException("Variabel lingkungan '" + matcher.group(1)
                        + "' belum diisi dan tidak punya nilai default.");
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement.trim()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static Optional<String> readLocal(Path file, Map<String, String> env) {
        if (!Files.isRegularFile(file)) {
            return Optional.empty();
        }
        try (InputStream in = Files.newInputStream(file)) {
            return apiUrlFrom(in, env);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca " + file + ": " + e.getMessage(), e);
        }
    }

    private static Optional<String> readBundled(Map<String, String> env) {
        try (InputStream in = AppConfig.class.getResourceAsStream(CLASSPATH_FILE)) {
            return in == null ? Optional.empty() : apiUrlFrom(in, env);
        } catch (IOException e) {
            throw new UncheckedIOException("Gagal membaca application.properties: " + e.getMessage(), e);
        }
    }

    private static Optional<String> apiUrlFrom(InputStream in, Map<String, String> env) throws IOException {
        Properties properties = new Properties();
        properties.load(in);
        String value = properties.getProperty(API_URL_KEY);
        if (value == null || value.isBlank()) {
            return Optional.empty();
        }
        String resolved = resolve(value.trim(), env);
        return resolved.isBlank() ? Optional.empty() : Optional.of(resolved);
    }
}
