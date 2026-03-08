package my.demo.model;

import lombok.Getter;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Reservation {

    private final UUID id;
    private final CarType carType;
    private final Instant start;
    private final Instant end;

    public Reservation(UUID id, CarType carType, Instant start, Instant end) {
        this.id = Objects.requireNonNull(id, "id cannot be null");
        this.carType = Objects.requireNonNull(carType, "carType cannot be null");
        this.start = Objects.requireNonNull(start, "start cannot be null");
        this.end = Objects.requireNonNull(end, "end cannot be null");

        if (!end.isAfter(start)) {
            // start >= end
            throw new IllegalArgumentException("end must be after start");
        }
    }

    public boolean overlaps(Instant otherStart, Instant otherEnd) {
        Objects.requireNonNull(otherStart);
        Objects.requireNonNull(otherEnd);

        return start.isBefore(otherEnd) && otherStart.isBefore(end);
    }
}
