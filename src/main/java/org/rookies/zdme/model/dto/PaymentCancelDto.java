package org.rookies.zdme.model.dto;

import lombok.Data;

@Data
public class PaymentCancelDto {
    private String PaymentKey;
    private String cancelReason;
    private Long cancelAmount;
}
