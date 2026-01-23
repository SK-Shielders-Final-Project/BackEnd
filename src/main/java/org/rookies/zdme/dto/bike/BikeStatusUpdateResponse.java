package org.rookies.zdme.dto.bike;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BikeStatusUpdateResponse {
    private Long bike_id;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated_at;
}
