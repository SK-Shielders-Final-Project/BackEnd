package org.rookies.zdme.chat.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ChatRequestDto(
        MessageDetail message // 👈 기존 String message를 MessageDetail 객체로 변경!
) {
    public record MessageDetail(
            String role,

            @JsonProperty("user_id") // JSON의 user_id를 자바 필드에 매핑
            Long userId,

            String content
    ) {}
}