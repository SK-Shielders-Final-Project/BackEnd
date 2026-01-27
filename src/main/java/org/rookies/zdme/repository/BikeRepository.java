package org.rookies.zdme.repository;

import org.rookies.zdme.model.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, Long> {
}
