package my.demo.dto;

import io.micronaut.serde.annotation.Serdeable;
import my.demo.model.CarType;

import java.time.Instant;

@Serdeable
public record AvailabilityResponse(
    CarType carType,
    Instant start,
    int numOfDays,
    boolean available
) {}