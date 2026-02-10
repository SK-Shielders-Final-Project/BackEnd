package org.rookies.zdme.dto.security;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class VerifyRequestDto {
    private String nonce;
    private String binaryHash;
    private String signatureHash;
}
