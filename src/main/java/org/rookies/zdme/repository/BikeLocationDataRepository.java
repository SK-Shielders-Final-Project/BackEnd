package org.rookies.zdme.repository; // 패키지 경로 변경

import org.rookies.zdme.domain.BikeLocationData; // 새로 생성된 엔티티 임포트
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BikeLocationDataRepository extends JpaRepository<BikeLocationData, Long> {
}
