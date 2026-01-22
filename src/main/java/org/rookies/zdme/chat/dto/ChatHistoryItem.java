package org.rookies.zdme.chat.dto;

public record ChatHistoryItem(
        String role,     // "user" | "assistant" (권장)
        String content
) {}

