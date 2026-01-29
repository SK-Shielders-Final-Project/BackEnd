package org.rookies.zdme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.User;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@AllArgsConstructor
public class UserUpdateResponse {

    private Long user_id;
    private String username;
    private String email;
    private String phone;
    private Integer admin_lev;
    private String created_at;
    private String updated_at;

    public static UserUpdateResponse fromEntity(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserUpdateResponse.builder()
                .user_id(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .admin_lev(user.getAdminLevel())
                .created_at(user.getCreatedAt() != null ? user.getCreatedAt().format(formatter) : null)
                .updated_at(user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null)
                .build();
    }
}
