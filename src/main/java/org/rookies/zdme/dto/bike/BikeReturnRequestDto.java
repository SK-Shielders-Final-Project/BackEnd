package org.rookies.zdme.dto.bike;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BikeReturnRequestDto {
    @JsonProperty("SERIAL_NUMBER")
    private String serialNumber;
    private Double latitude;
    private Double longitude;

    @NotNull(message = "fileId는 필수입니다.")
    private Long fileId;
}
