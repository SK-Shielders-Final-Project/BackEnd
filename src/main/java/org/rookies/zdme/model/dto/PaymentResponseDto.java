package org.rookies.zdme.model.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResponseDto {
    private Long paymentId;
    private Long userId;
    private Long totalPoint;
    private String paymentKey;
}
