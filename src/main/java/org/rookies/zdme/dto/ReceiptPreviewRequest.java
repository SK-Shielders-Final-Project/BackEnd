package org.rookies.zdme.dto;

import lombok.Data;

@Data
public class ReceiptPreviewRequest {
    private Long paymentId;
    private String userMemo;
}
