package org.rookies.zdme.service;

import lombok.RequiredArgsConstructor;
import org.rookies.zdme.model.dto.RentalRequestDto;
import org.rookies.zdme.model.dto.RentalResponseDto;
import org.rookies.zdme.model.entity.Bike;
import org.rookies.zdme.model.entity.Rental;
import org.rookies.zdme.model.entity.User;
import org.rookies.zdme.repository.BikeRepository;
import org.rookies.zdme.repository.RentalRepository;
import org.rookies.zdme.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RentalService {

    private final UserRepository userRepository;
    private final BikeRepository bikeRepository;
    private final RentalRepository rentalRepository;

    private static final long POINT_PER_HOUR = 1000L;

    public RentalResponseDto startRental(RentalRequestDto dto) {
        Long requiredPoint = dto.getHoursToUse() * 1000L;

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        Bike bike = bikeRepository.findById(dto.getBikeId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 자전거입니다."));

        try {
            user.updatePoint(-1 * requiredPoint);
        } catch (IllegalStateException e) {
            throw new RuntimeException("결제 실패 : " + e.getMessage());
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
}
