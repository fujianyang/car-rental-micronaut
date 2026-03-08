package my.demo.service;

import jakarta.inject.Singleton;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import lombok.extern.slf4j.Slf4j;
import my.demo.exception.NoCarAvailableException;
import my.demo.exception.ReservationNotFoundException;
import my.demo.model.CarType;
import my.demo.model.Reservation;
import my.demo.repository.ReservationRepository;

@Slf4j
@Singleton
public class CarRentalService {

    private final ReservationRepository reservationRepository;
    private final FleetInventory fleetInventory;
    private final Map<CarType, Lock> locksByType = new EnumMap<>(CarType.class);

    public CarRentalService(ReservationRepository reservationRepository,
                            FleetInventory fleetInventory)
    {
        this.reservationRepository = reservationRepository;
        this.fleetInventory = fleetInventory;

        for (CarType carType : CarType.values()) {
            locksByType.put(carType, new ReentrantLock());
        }
    }

    public Reservation reserve(CarType carType, Instant start, int numOfDays) {
        validateRequest(carType, start, numOfDays);
        Instant end = start.plus(numOfDays, ChronoUnit.DAYS);

        // lock only for the same type, can go parallel with reservations on other types
        Lock lock = locksByType.get(carType);
        lock.lock();

        log.trace("Lock acquired: {}", carType);

        try {

            if (!isAvailableInternal(carType, start, end)) {
                log.trace("Availability rejected: carType={}, start={}, end={}", carType, start, end);
                throw new NoCarAvailableException(
                    "No " + carType + " available for the requested time range"
                );
            }

            log.trace("Availability confirmed: carType={}, start={}, end={}", carType, start, end);

            Reservation reservation = new Reservation(
                UUID.randomUUID(),
                carType,
                start,
                end
            );

            reservationRepository.save(reservation);
            return reservation;
        } finally {
            lock.unlock();
            log.trace("Lock released: {}", carType);
        }
    }

    public Reservation getReservation(UUID id) {
        return reservationRepository.findById(id)
            .orElseThrow(() -> new ReservationNotFoundException("Reservation not found: " + id));
    }

    public boolean isAvailable(CarType carType, Instant start, int numOfDays) {
        validateRequest(carType, start, numOfDays);

        Instant end = start.plus(numOfDays, ChronoUnit.DAYS);

        // no need for lock here
        return isAvailableInternal(carType, start, end);
    }

    public List<Reservation> getAllReservations() {
        return reservationRepository.findAll();
    }

    private boolean isAvailableInternal(CarType carType, Instant start, Instant end) {
        List<Reservation> reservations = reservationRepository.findByCarType(carType);

        long overlappingCount = reservations.stream()
            .filter(existing -> existing.overlaps(start, end))
            .count();

        return overlappingCount < fleetInventory.getCapacity(carType);
    }

    private void validateRequest(CarType carType, Instant start, int numOfDays) {
        if (carType == null) {
            throw new IllegalArgumentException("carType cannot be null");
        }
        if (start == null) {
            throw new IllegalArgumentException("start cannot be null");
        }
        if (numOfDays <= 0) {
            throw new IllegalArgumentException("numOfDays must be greater than 0");
        }

        if (!start.isAfter(Instant.now())) {
            throw new IllegalArgumentException("Reservation start time must be in the future");
        }
    }
}
