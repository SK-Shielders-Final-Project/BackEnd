package org.rookies.zdme.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GiftResponseDto {
    private String encryptedResult;
}
