package my.demo.repository;

import my.demo.model.CarType;
import my.demo.model.Reservation;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReservationRepository {

    void save(Reservation reservation);

    List<Reservation> findByCarType(CarType carType);

    List<Reservation> findAll();

    Optional<Reservation> findById(UUID id);
}
