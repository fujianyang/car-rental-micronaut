package my.demo.repository;

import jakarta.inject.Singleton;
import my.demo.model.CarType;
import my.demo.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Singleton
public class InMemoryReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();

    @Override
    public void save(Reservation reservation) {
        reservations.add(reservation);
    }

    @Override
    public List<Reservation> findByCarType(CarType carType) {
        return reservations.stream()
            .filter(r -> r.getCarType() == carType)
            .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public Optional<Reservation> findById(UUID id) {
        return reservations.stream()
            .filter(r -> r.getId().equals(id))
            .findFirst();
    }
}
