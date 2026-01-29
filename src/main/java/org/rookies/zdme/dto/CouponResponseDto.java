package org.rookies.zdme.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CouponResponseDto {
    private Long userId;
    private Long totalPoint;
    private Long rechargedPoint;
}
