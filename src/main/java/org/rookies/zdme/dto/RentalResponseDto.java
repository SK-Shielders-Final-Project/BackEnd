package org.rookies.zdme.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RentalResponseDto {
    private Long userId;
    private Long bikeId;
    private Long currentPoint;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
}
