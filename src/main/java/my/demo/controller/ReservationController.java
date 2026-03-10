package my.demo.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Controller;
import lombok.extern.slf4j.Slf4j;
import my.demo.api.ReservationApi;
import my.demo.dto.AvailabilityResponse;
import my.demo.dto.CreateReservationRequest;
import my.demo.dto.ReservationResponse;
import my.demo.model.CarType;
import my.demo.model.Reservation;
import my.demo.service.CarRentalService;

import java.net.URI;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller(ReservationApi.BASE_PATH)
public class ReservationController implements ReservationApi {

    private final CarRentalService carRentalService;

    public ReservationController(CarRentalService carRentalService) {
        this.carRentalService = carRentalService;
    }

    public HttpResponse<ReservationResponse> createReservation(CreateReservationRequest request)
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

    public AvailabilityResponse checkAvailability(
        CarType carType,
        Instant start,
        int numOfDays)
    {
        boolean available = carRentalService.isAvailable(carType, start, numOfDays);

        return new AvailabilityResponse(
            carType,
            start,
            numOfDays,
            available
        );
    }

    public List<ReservationResponse> listReservations() {
        return carRentalService.getAllReservations().stream()
            .map(ReservationResponse::from)
            .toList();
    }

    public ReservationResponse getReservation(UUID id) {
        return ReservationResponse.from(carRentalService.getReservation(id));
    }

    private URI reservationLocation(UUID id) {
        return URI.create(ReservationApi.BASE_PATH + "/" + id);
    }
}