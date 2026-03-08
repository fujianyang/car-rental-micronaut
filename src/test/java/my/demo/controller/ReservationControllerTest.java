package my.demo.controller;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import jakarta.inject.Inject;
import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import my.demo.dto.AvailabilityResponse;
import my.demo.dto.CreateReservationRequest;
import my.demo.dto.ReservationResponse;
import my.demo.model.CarType;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class ReservationControllerTest {

    @Inject
    EmbeddedServer server;

    @Inject
    @Client("/")
    HttpClient client;

    @BeforeEach
    void printServerInfo() {
        System.out.println("Micronaut test server: " + server.getURI());
    }

    @Test
    void shouldCreateAndGetReservation() {
        CreateReservationRequest request = new CreateReservationRequest(
            my.demo.model.CarType.SUV,
            Instant.parse("2027-03-10T10:00:00Z"),
            2
        );

        HttpRequest<CreateReservationRequest> httpRequest =
            HttpRequest.POST("/reservations", request);

        var response = client.toBlocking().exchange(httpRequest, ReservationResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatus());
        assertNotNull(response.body());
        assertEquals(CarType.SUV, response.body().carType());

        assertTrue(response.getHeaders().contains("Location"));
        String location = response.getHeaders().get("Location");
        assertTrue(location.startsWith("/reservations/"));

        var reservation =
            client.toBlocking().exchange(HttpRequest.GET(location), ReservationResponse.class);

        assertEquals(HttpStatus.OK, reservation.getStatus());
        assertEquals(CarType.SUV, reservation.body().carType());
    }

    @Test
    void shouldRejectWhenCapacityExceeded() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        var req1 = new CreateReservationRequest(my.demo.model.CarType.VAN, start, 2);
        var req2 = new CreateReservationRequest(my.demo.model.CarType.VAN, start, 2);

        client.toBlocking().exchange(HttpRequest.POST("/reservations", req1), ReservationResponse.class);

        HttpClientResponseException ex = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/reservations", req2),
                String.class
            )
        );

        printHttpException(ex);

        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
    }

    @Test
    void shouldRejectInvalidCreateReservationRequest() {
        String invalidJson = """
        {
          "carType": "SUV",
          "start": "2027-03-10T10:00:00Z",
          "numberOfDays": 0
        }
        """;

        HttpClientResponseException ex = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.POST("/reservations", invalidJson)
                    .contentType("application/json"),
                String.class
            )
        );

        printHttpException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldReturnAvailability() {
        String url = "/reservations/availability?carType=SEDAN&start=2027-03-10T10:00:00Z&numOfDays=2";

        try {
            AvailabilityResponse response =
                client.toBlocking().retrieve(HttpRequest.GET(url), AvailabilityResponse.class);

            assertNotNull(response);
            assertEquals(my.demo.model.CarType.SEDAN, response.carType());
        } catch (HttpClientResponseException ex) {
            printHttpException(ex);
            throw ex;
        }
    }

    @Test
    void shouldRejectInvalidAvailabilityRequest() {
        String url = "/reservations/availability?carType=SEDAN&start=2027-03-10T10:00:00Z&numberOfDays=0";

        HttpClientResponseException ex = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().exchange(
                HttpRequest.GET(url),
                String.class
            )
        );

        printHttpException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    @Test
    void shouldListReservations() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        client.toBlocking().exchange(
            HttpRequest.POST("/reservations",
                new CreateReservationRequest(CarType.SUV, start, 2)),
            ReservationResponse.class
        );

        ReservationResponse[] responses = client.toBlocking()
            .retrieve(HttpRequest.GET("/reservations"), ReservationResponse[].class);

        assertNotNull(responses);
        assertTrue(responses.length >= 1);
    }

    @Test
    void shouldReturnNotFoundForMissingReservation() {
        UUID missingId = UUID.fromString("00000000-0000-0000-0000-000000000000");

        HttpClientResponseException ex = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(
                HttpRequest.GET("/reservations/" + missingId),
                ReservationResponse.class
            )
        );

        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
    }

    @Test
    void shouldReturnBadRequestForBadReservationId() {

        String missingId = "not-a-uuid";

        HttpClientResponseException ex = assertThrows(
            HttpClientResponseException.class,
            () -> client.toBlocking().retrieve(
                HttpRequest.GET("/reservations/" + missingId),
                ReservationResponse.class
            )
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }

    private void printHttpException(HttpClientResponseException ex) {
        System.out.println("Status = " + ex.getStatus());
        System.out.println("Body   = " + ex.getResponse().getBody(String.class).orElse("<empty>"));
        ex.printStackTrace();
    }

}