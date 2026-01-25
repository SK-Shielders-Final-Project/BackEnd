package org.rookies.zdme.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.rookies.zdme.model.entity.User;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponse {
    @JsonProperty("user_id")
    private Long userId;

    private String username;
    private String name;
    private String email;
    private String phone;

    @JsonProperty("admin_lev")
    private Integer adminLev;

    @JsonProperty("total_point")
    private Integer totalPoint;

    @JsonProperty("created_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    public static SignupResponse fromEntity(User user) {
        return SignupResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .adminLev(user.getAdminLevel())
                .totalPoint(user.getTotalPoint())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
