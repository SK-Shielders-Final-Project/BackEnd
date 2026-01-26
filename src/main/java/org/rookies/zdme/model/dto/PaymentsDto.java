package org.rookies.zdme.model.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PaymentsDto {
    private Long userId;
    private Long amount;
    private String orderId;
    private String paymentKey;
    private String paymentMethod;
    private LocalDateTime createAt;
    private String status;
    private Long remainAmount;
}
