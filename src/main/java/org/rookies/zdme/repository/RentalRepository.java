package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.Rental;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RentalRepository extends JpaRepository<Rental, Long> {

}
