package com.credit.service;

import com.credit.exception.InvalidLoanException;
import com.credit.exception.LoanApiException;
import com.credit.factories.LoanRequestFactory;
import com.credit.model.LoanRequest;
import com.credit.model.VehicleCondition;
import com.credit.model.VehicleType;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoanApiClientTest {

    private static final String VALID_BODY = "{\"vehicleType\":\"Mobil\",\"vehicleCondition\":\"Baru\","
            + "\"vehicleYear\":2025,\"totalLoanAmount\":1000000000,\"loanTenure\":6,\"downPayment\":500000000}";

    private final LoanRequestFactory factory =
            new LoanRequestFactory(Clock.fixed(Instant.parse("2025-06-01T00:00:00Z"), ZoneOffset.UTC));

    private HttpServer server;
    private int status;
    private String body;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/loan", exchange -> {
            byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(status, bytes.length == 0 ? -1 : bytes.length);
            if (bytes.length > 0) {
                exchange.getResponseBody().write(bytes);
            }
            exchange.close();
        });
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    private LoanApiClient client() {
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/loan";
        return new LoanApiClient(HttpClient.newHttpClient(), factory, url);
    }

    @Test
    void fetchMapsJsonToLoanRequest() {
        status = 200;
        body = VALID_BODY;
        LoanRequest r = client().fetch();
        assertEquals(VehicleType.MOBIL, r.vehicleType());
        assertEquals(VehicleCondition.BARU, r.condition());
        assertEquals(2025, r.year());
        assertEquals(6, r.tenorYears());
        assertEquals(0, r.downPayment().compareTo(new java.math.BigDecimal("500000000")));
    }

    @Test
    void fetchFailsOnNon200() {
        status = 500;
        body = "boom";
        assertThrows(LoanApiException.class, () -> client().fetch());
    }

    @Test
    void fetchFailsOnEmptyBody() {
        status = 200;
        body = "";
        assertThrows(LoanApiException.class, () -> client().fetch());
    }

    @Test
    void fetchFailsOnInvalidJson() {
        status = 200;
        body = "{not json";
        assertThrows(LoanApiException.class, () -> client().fetch());
    }

    @Test
    void fetchFailsOnMissingField() {
        status = 200;
        body = "{\"vehicleType\":\"Mobil\"}";
        assertThrows(LoanApiException.class, () -> client().fetch());
    }

    @Test
    void fetchFailsOnInvalidBusinessValue() {
        status = 200;
        body = VALID_BODY.replace("\"loanTenure\":6", "\"loanTenure\":9");
        assertThrows(InvalidLoanException.class, () -> client().fetch());
    }

    @Test
    void fetchFailsWhenServerDown() {
        server.stop(0);
        assertThrows(LoanApiException.class, () -> client().fetch());
    }
}
