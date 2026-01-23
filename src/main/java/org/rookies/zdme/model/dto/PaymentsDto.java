package org.rookies.zdme.model.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentsDto {
    private Long userId;
    private Long amount;
    private Long orderId;
    private String paymentKey;
    private String paymentMethod;
    private LocalDateTime createAt;
}
