package my.demo.dto;

import io.micronaut.serde.annotation.Serdeable;
import my.demo.model.CarType;
import my.demo.model.Reservation;

import java.time.Instant;
import java.util.UUID;

@Serdeable
public record ReservationResponse(
    UUID id,
    CarType carType,
    Instant start,
    Instant end)
{
    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getCarType(),
            reservation.getStart(),
            reservation.getEnd()
        );
    }
}