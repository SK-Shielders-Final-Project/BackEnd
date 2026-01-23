package org.rookies.zdme.dto.admin;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffAdminLevelUpdateResponse {
    private Long user_id;
    private String username;
    private Integer admin_level;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updated_at;
}
