package org.rookies.zdme.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.User;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@AllArgsConstructor
public class UserInfoPartialUpdateResponse {
    private Long user_id;
    private String username;
    private String name;
    private String updated_at;

    public static UserInfoPartialUpdateResponse fromEntity(User user) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return UserInfoPartialUpdateResponse.builder()
                .user_id(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .updated_at(user.getUpdatedAt() != null ? user.getUpdatedAt().format(formatter) : null)
                .build();
    }
}
