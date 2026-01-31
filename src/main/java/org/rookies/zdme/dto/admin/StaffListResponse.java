package org.rookies.zdme.dto.admin;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class StaffListResponse {
    private Long user_id;
    private String email;
    private Integer admin_level;
    private String user_name; // Long -> String으로 수정 추천
    private String name;      // Long -> String으로 수정 추천
    private String phone;     // Integer -> String으로 수정 추천 (010 때문)

}
