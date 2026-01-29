package org.rookies.zdme.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffListResponse {
    private Long user_id;
    private String email;
    private Integer admin_level; // 0 / 1 / 2
}
