package org.rookies.zdme.repository;

import java.util.List;
import org.rookies.zdme.model.entity.Bike;
import org.rookies.zdme.model.enums.BikeStatus; // BikeStatus 임포트 추가
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BikeRepository extends JpaRepository<Bike, Long> {
    List<Bike> findAllByStatusOrderByBikeIdAsc(BikeStatus status); // String -> BikeStatus 변경
    Optional<Bike> findBySerialNumber(String serialNumber);
}
