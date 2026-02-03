package org.rookies.zdme.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class GiftHistoryResponseDto {
    private String senderName;
    private String receiverName;
    private Long amount;
    private LocalDateTime createdAt;
}
