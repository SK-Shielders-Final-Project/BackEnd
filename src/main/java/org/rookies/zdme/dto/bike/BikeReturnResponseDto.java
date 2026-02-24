package org.rookies.zdme.dto.bike;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikeReturnResponseDto {
    private Double latitude;
    private Double longitude;
    private LocalDateTime updatedAt;
}
