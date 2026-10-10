package com.credit.service;

import com.credit.config.AppConfig;
import com.credit.constant.ErrorMessage;
import com.credit.exception.LoanApiException;
import com.credit.factories.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class LoanApiClient implements LoanDataProvider {

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
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
        return new LoanApiClient(client, factory, AppConfig.apiUrl());
    }

    @Override
    public LoanRequest fetch() {
        HttpRequest request = HttpRequest.newBuilder(uri).timeout(Duration.ofSeconds(15)).GET().build();
        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LoanApiException(ErrorMessage.API_INTERRUPTED, e);
        } catch (IOException e) {
            throw new LoanApiException(ErrorMessage.API_CALL_FAILED.formatted(e.getMessage()), e);
        }
        if (response.statusCode() != HTTP_OK) {
            throw new LoanApiException(ErrorMessage.API_HTTP_STATUS.formatted(response.statusCode()));
        }
        return toRequest(response.body());
    }

    LoanRequest toRequest(String body) {
        JsonNode root;
        try {
            root = objectMapper.readTree(body);
        } catch (IOException e) {
            throw new LoanApiException(ErrorMessage.API_INVALID_JSON, e);
        }
        if (!root.isObject()) {
            throw new LoanApiException(ErrorMessage.API_NOT_AN_OBJECT);
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
            throw new LoanApiException(ErrorMessage.API_FIELD_MISSING.formatted(field));
        }
        return node.asText();
    }
}
