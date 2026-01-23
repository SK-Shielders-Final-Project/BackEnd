package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.Rental;
import org.rookies.zdme.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RentalRepository extends JpaRepository<Rental, Long> {
    List<Rental> findAllByUser(User user);
}
