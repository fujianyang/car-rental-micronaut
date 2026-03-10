package my.demo.api;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import my.demo.dto.AvailabilityResponse;
import my.demo.dto.CreateReservationRequest;
import my.demo.dto.ReservationResponse;
import my.demo.model.CarType;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ReservationApi {

    String BASE_PATH = "/reservations";

    @Post
    HttpResponse<ReservationResponse> createReservation(
        @Body @Valid CreateReservationRequest request
    );

    @Get("/availability")
    AvailabilityResponse checkAvailability(
        @QueryValue @NotNull CarType carType,
        @QueryValue @NotNull Instant start,
        @QueryValue @Positive int numOfDays
    );

    @Get
    List<ReservationResponse> listReservations();

    @Get("/{id}")
    ReservationResponse getReservation(@PathVariable UUID id);
}
