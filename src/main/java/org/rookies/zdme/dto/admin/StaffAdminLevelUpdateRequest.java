package org.rookies.zdme.dto.admin;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StaffAdminLevelUpdateRequest {
    private Long user_id;
    private Integer admin_level; // 0/1/2
}
