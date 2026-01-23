package org.rookies.zdme.model.dto;

import lombok.Data;

@Data
public class RentalRequestDto {
    private Long userId;
    private Integer hoursToUse;
    private Long bikeId;
}
