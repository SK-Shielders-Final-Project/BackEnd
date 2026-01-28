package org.rookies.zdme.dto.bike;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BikeResponse {
    private Long bike_id;
    private String serial_number;
    private String model_name;

    private Integer status_code;   // 0/1/2
    private String status;         // "사용중"/"가용"/"고장"

    private Double latitude;
    private Double longitude;
}
