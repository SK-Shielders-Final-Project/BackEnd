package org.rookies.zdme.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.TemporalType;
import lombok.RequiredArgsConstructor;
import net.sf.jsqlparser.expression.TimestampValue;
import org.rookies.zdme.dto.RentalRequestDto;
import org.rookies.zdme.dto.RentalResponseDto;
import org.rookies.zdme.dto.RentalsDto;
import org.rookies.zdme.model.entity.Bike;
import org.rookies.zdme.model.entity.Rental;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.BikeRepository;
import org.rookies.zdme.repository.RentalRepository;
import org.rookies.zdme.repository.UserRepository;
import org.rookies.zdme.security.SecurityUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final RentalRepository rentalRepository;

    private static final long POINT_PER_HOUR = 1000L;

    @PersistenceContext
    private EntityManager entityManager;

    public RentalResponseDto startRental(RentalRequestDto dto) {
        long requiredPoint = dto.getHoursToUse() * 1000L;

        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("사용자가 존재하지 않습니다."));

        Bike bike = bikeRepository.findById(dto.getBikeId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 자전거입니다."));

        try {
            user.updatePoint(-1 * requiredPoint);
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("결제 실패 : " + e.getMessage());
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expectedEndTime = now.plusHours(dto.getHoursToUse());

        Rental rental = Rental.builder()
                .startTime(now)
                .endTime(expectedEndTime)
                .user(user)
                .bike(bike)
                .createdAt(now)
                .totalDistance(0.0)
                .build();

        rentalRepository.save(rental);

        return RentalResponseDto.builder()
                .bikeId(bike.getBikeId())
                .userId(user.getUserId())
                .currentPoint(user.getTotalPoint())
                .startTime(rental.getStartTime())
                .endTime(rental.getEndTime())
                .build();
    }

    public List<RentalsDto> getRentals(){
        String username = SecurityUtil.getCurrentUsername();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        List<Rental> rentals = rentalRepository.findAllByUserOrderByCreatedAtDesc(user);
        return rentals.stream()
                .map(r -> RentalsDto.builder()
                        .userId(r.getUser().getUserId())
                        .bikeId(r.getBike().getBikeId())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RentalsDto> searchRentals(LocalDate start, LocalDate end, String bikeId) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT * FROM rentals r ");
        sql.append("WHERE r.start_time BETWEEN :startDate AND :endDate ");

        if (bikeId != null && !bikeId.isEmpty()) {
            sql.append("AND (TO_CHAR(r.bike_id) LIKE '%" + bikeId + "%') ");
        }

        sql.append("ORDER BY r.start_time DESC");

        Query query = entityManager.createNativeQuery(sql.toString(), Rental.class);

        LocalDateTime startTime = start.atStartOfDay();
        LocalDateTime endTime = end.atTime(LocalTime.MAX);
        query.setParameter("startDate", Timestamp.valueOf(startTime));
        query.setParameter("endDate", Timestamp.valueOf(endTime));

        List<Rental> results = query.getResultList();

        return results.stream()
                .map(r -> RentalsDto.builder()
                        .userId(r.getUser().getUserId())
                        .bikeId(r.getBike().getBikeId())
                        .startTime(r.getStartTime())
                        .endTime(r.getEndTime())
                        .build())
                .collect(Collectors.toList());
    }
}
