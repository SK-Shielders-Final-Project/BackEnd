package org.rookies.zdme.dto.bike;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BikeStatusUpdateRequest {
    private Long bike_id;
    private String status; // "사용중" / "가용" / "고장"
}
