package my.demo.controller;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.extern.slf4j.Slf4j;
import my.demo.dto.AvailabilityResponse;
import my.demo.dto.CreateReservationRequest;
import my.demo.dto.ReservationResponse;
import my.demo.model.CarType;
import my.demo.model.Reservation;
import my.demo.service.CarRentalService;

@Slf4j
@Controller("/reservations")
public class ReservationController {

    private final CarRentalService carRentalService;

    public ReservationController(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
    }

    @Post
    public HttpResponse<ReservationResponse> createReservation(
        @Body @Valid CreateReservationRequest request)
    {
        log.debug("Creating reservation: carType={}, start={}, days={}",
            request.carType(),
            request.start(),
            request.numOfDays()
        );

        Reservation reservation = carRentalService.reserve(
            request.carType(),
            request.start(),
            request.numOfDays()
        );

        ReservationResponse body = ReservationResponse.from(reservation);
        URI location = reservationLocation(body.id());

        log.debug("Reservation created: carType={}, start={}, end={}, location={}",
            body.carType(), body.start(), body.end(), location
        );

        return HttpResponse.created(body)
            .headers(
                headers -> headers.location(location));
    }

    @Get("/availability")
    public AvailabilityResponse checkAvailability(
        @QueryValue @NotNull CarType carType,
        @QueryValue @NotNull Instant start,
        @QueryValue @Positive int numOfDays)
    {
        boolean available = carRentalService.isAvailable(carType, start, numOfDays);

        return new AvailabilityResponse(
            carType,
            start,
            numOfDays,
            available
        );
    }

    @Get
    public List<ReservationResponse> listReservations() {
        return carRentalService.getAllReservations().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    @Get("/{id}")
    public ReservationResponse getReservation(@PathVariable UUID id) {
        return ReservationResponse.from(carRentalService.getReservation(id));
    }

    private URI reservationLocation(UUID id) {
        return URI.create("/reservations/" + id);
    }
}