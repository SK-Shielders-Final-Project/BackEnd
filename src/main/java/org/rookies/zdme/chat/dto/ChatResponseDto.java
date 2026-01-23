package org.rookies.zdme.chat.dto;

public record  ChatResponseDto(
        @jakarta.validation.constraints.NotNull Long userId,
        String assistantMessage,
        String model
) {}
