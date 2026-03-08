package my.demo.dto;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import my.demo.model.CarType;

import java.time.Instant;

@Serdeable
public record CreateReservationRequest(
    @NotNull CarType carType,
    @NotNull Instant start,
    @Positive int numOfDays
) {}