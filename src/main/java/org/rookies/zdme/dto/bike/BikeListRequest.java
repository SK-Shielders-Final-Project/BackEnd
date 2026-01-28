package org.rookies.zdme.dto.bike;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BikeListRequest {
    private Integer status; // 0: 사용중, 1: 가용, 2: 고장(수리중)
}
