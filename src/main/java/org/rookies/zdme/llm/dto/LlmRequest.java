package org.rookies.zdme.llm.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record LlmRequest(
        @JsonProperty("message")
        Message message
) {
    public record Message(
            String role,
            @JsonProperty("user_id") Long userId,
            String content
    ) {}
}
