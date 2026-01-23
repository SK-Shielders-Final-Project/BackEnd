package org.rookies.zdme.chat.service;

import org.rookies.zdme.chat.dto.ChatRequestDto;
import org.rookies.zdme.chat.dto.ChatResponseDto;
import org.rookies.zdme.llm.client.LlmClient;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final LlmClient llmClient;

    public ChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public ChatResponseDto chat(ChatRequestDto req) {

        // 1️⃣ LLM 요청 생성 (userId + message)
        LlmRequest llmRequest = new LlmRequest(
                List.of(new LlmRequest.Message(
                        "user",
                        req.userId(),
                        req.message()
                ))
        );

        // 2️⃣ LLM 호출
        var llmResponse = llmClient.generate(llmRequest);

        // 3️⃣ userId 그대로 응답에 포함
        return new ChatResponseDto(
                req.userId(),
                llmResponse.text(),
                llmResponse.model()
        );
    }
}
