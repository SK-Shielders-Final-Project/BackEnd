package org.rookies.zdme.dto.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@AllArgsConstructor
public class VerifyResponseDto {
    private String integrityToken;
}
