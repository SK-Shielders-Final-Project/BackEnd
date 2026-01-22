package org.rookies.zdme.llm.dto;

import java.util.List;

public record LlmRequest(
        List<Message> messages
) {
    public record Message(String role, String content) {}
}
