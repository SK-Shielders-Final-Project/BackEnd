package org.rookies.zdme.dto.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppIntegrityResponse {
    @JsonProperty("is_valid")
    private boolean is_valid;
}
