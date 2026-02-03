package org.rookies.zdme.dto.app;

import lombok.Data;

@Data
public class AppIntegrityRequest {
    private String signature_hash;
    private String binary_hash;
}
