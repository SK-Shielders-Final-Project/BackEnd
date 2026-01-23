package org.rookies.zdme.model.dto;

import lombok.Data;

@Data
public class PaymentRequestDto {
    private String paymentKey;
    private String orderId;
    private Long amount;
}
