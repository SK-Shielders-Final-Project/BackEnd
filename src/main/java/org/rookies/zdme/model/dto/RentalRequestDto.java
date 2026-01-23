package org.rookies.zdme.model.dto;

import lombok.Data;

@Data
public class RentalRequestDto {
    private Integer hoursToUse;
    private Long bikeId;
}
