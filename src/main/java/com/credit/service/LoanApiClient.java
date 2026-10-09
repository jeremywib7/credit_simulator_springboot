package com.credit.service;

import com.credit.exception.LoanApiException;
import com.credit.factory.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LoanApiClient {

    public static final String DEFAULT_URL = "https://run.mocky.io/v3/9108b1da-beec-409e-ae14-e8091955666c";

    private static final int HTTP_OK = 200;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final LoanRequestFactory factory;
    private final URI uri;

    public LoanApiClient(HttpClient httpClient, LoanRequestFactory factory, String url) {
        this.httpClient = httpClient;
        this.factory = factory;
        this.uri = URI.create(url);
    }

    public static LoanApiClient createDefault(LoanRequestFactory factory) {
        String url = System.getenv().getOrDefault("CREDIT_API_URL", DEFAULT_URL);
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        return new LoanApiClient(client, factory, url);
    }

    public LoanRequest fetch() {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoanApiException("Pemanggilan API terputus.", e);
        } catch (IOException e) {
            throw new LoanApiException("Gagal memanggil API: " + e.getMessage(), e);
        }
        if (response.statusCode() != HTTP_OK) {
            throw new LoanApiException("API membalas HTTP " + response.statusCode() + ".");
        }
        return toRequest(response.body());
    }

    LoanRequest toRequest(String body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (IOException e) {
            throw new LoanApiException("Respons API bukan JSON valid.", e);
        }
        if (root == null || !root.isObject()) {
            throw new LoanApiException("Respons API kosong atau bukan objek JSON.");
        }
        return factory.create(
                text(root, "vehicleType"),
                text(root, "vehicleCondition"),
                text(root, "vehicleYear"),
                text(root, "totalLoanAmount"),
                text(root, "loanTenure"),
                text(root, "downPayment"));
    }

    private String text(JsonNode root, String field) {
        JsonNode node = root.get(field);
        if (node == null || node.isNull()) {
            throw new LoanApiException("Field '" + field + "' tidak ada pada respons API.");
        }
        return node.asText();
    }
}
