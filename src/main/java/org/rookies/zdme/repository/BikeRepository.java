package org.rookies.zdme.repository;

import java.util.List;
import org.rookies.zdme.model.entity.Bike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    List<Bike> findAllByStatusOrderByBikeIdAsc(String status);
}
