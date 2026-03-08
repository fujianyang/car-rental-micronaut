package my.demo.service;

import my.demo.exception.NoCarAvailableException;
import my.demo.exception.ReservationNotFoundException;
import my.demo.model.CarType;
import my.demo.model.Reservation;
import my.demo.repository.InMemoryReservationRepository;
import my.demo.repository.ReservationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CarRentalServiceTest {

    private CarRentalService service;

    @BeforeEach
    void setUp() {
        ReservationRepository repository = new InMemoryReservationRepository();
        FleetInventory fleetInventory = new FleetInventory(
            Map.of(
                CarType.SEDAN, 3,
                CarType.SUV, 2,
                CarType.VAN, 1
            )
        );
        service = new CarRentalService(repository, fleetInventory);
    }

    @Test
    void shouldReserveWhenCarIsAvailable() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        Reservation reservation = service.reserve(CarType.SUV, start, 2);

        assertNotNull(reservation.getId());
        assertEquals(CarType.SUV, reservation.getCarType());
        assertEquals(start, reservation.getStart());
        assertEquals(start.plus(2, ChronoUnit.DAYS), reservation.getEnd());

        assertNotNull(service.getReservation(reservation.getId()));
    }

    @Test
    void shouldAllowReservationsUpToCapacity() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        service.reserve(CarType.SUV, start, 2);
        service.reserve(CarType.SUV, start, 2);

        assertFalse(service.isAvailable(CarType.SUV, start, 2));
    }

    @Test
    void shouldRejectReservationWhenCapacityExceeded() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        service.reserve(CarType.VAN, start, 2);

        NoCarAvailableException ex = assertThrows(
            NoCarAvailableException.class,
            () -> service.reserve(CarType.VAN, start, 2)
        );

        assertEquals("No VAN available for the requested time range", ex.getMessage());
    }

    @Test
    void shouldAllowBackToBackReservations() {
        Instant firstStart = Instant.parse("2027-03-10T10:00:00Z");
        Instant secondStart = Instant.parse("2027-03-11T10:00:00Z");

        service.reserve(CarType.VAN, firstStart, 1);

        assertTrue(service.isAvailable(CarType.VAN, secondStart, 1));
        assertDoesNotThrow(() -> service.reserve(CarType.VAN, secondStart, 1));
    }

    @Test
    void shouldNotMixDifferentCarTypes() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        service.reserve(CarType.VAN, start, 2);

        assertTrue(service.isAvailable(CarType.SEDAN, start, 2));
        assertTrue(service.isAvailable(CarType.SUV, start, 2));
    }

    @Test
    void shouldReturnAllReservations() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        service.reserve(CarType.SEDAN, start, 2);
        service.reserve(CarType.SUV, start.plus(1, ChronoUnit.DAYS), 3);

        assertEquals(2, service.getAllReservations().size());
    }

    @Test
    void shouldThrowReservationNotFoundExceptionForMissingReservation() {
        UUID missingId = UUID.randomUUID();
        assertThrows(
            ReservationNotFoundException.class,
            () -> service.getReservation(missingId)
        );
    }

    @Test
    void shouldRejectNullCarType() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.reserve(null, start, 2)
        );

        assertEquals("carType cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNullStart() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.reserve(CarType.SEDAN, null, 2)
        );

        assertEquals("start cannot be null", ex.getMessage());
    }

    @Test
    void shouldRejectNonPositiveNumOfDays() {
        Instant start = Instant.parse("2027-03-10T10:00:00Z");

        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> service.reserve(CarType.SEDAN, start, 0)
        );

        assertEquals("numOfDays must be greater than 0", ex.getMessage());
    }
}