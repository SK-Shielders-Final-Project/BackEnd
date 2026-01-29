package org.rookies.zdme.dto;

import lombok.Data;

@Data
public class PaymentCancelDto {
    private String PaymentKey;
    private String cancelReason;
    private Long cancelAmount;
}
