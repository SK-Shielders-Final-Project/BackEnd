package org.rookies.zdme.chat.dto;

public record  ChatResponseDto(
        String conversationId,
        String assistantMessage,
        String model
) {}
