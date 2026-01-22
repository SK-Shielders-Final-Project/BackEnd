package org.rookies.zdme.chat.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ChatRequestDto(
        String conversationId,

        @NotBlank(message = "message는 비어 있을 수 없습니다.")
        @Size(max = 2000, message = "message는 최대 2000자까지 허용됩니다.")
        String message,

        // DB 저장 안 하므로, 클라이언트가 최근 대화 내역을 같이 보내는 구조
        List<ChatHistoryItem> history
) {}
