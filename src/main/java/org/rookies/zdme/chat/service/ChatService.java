package org.rookies.zdme.chat.service;

import org.rookies.zdme.chat.dto.ChatRequestDto;
import org.rookies.zdme.chat.dto.ChatResponseDto;
import org.rookies.zdme.llm.client.LlmClient;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final LlmClient llmClient;

    public ChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public ChatResponseDto chat(ChatRequestDto req) {

        // 1️⃣ LLM 요청 생성 (req.message() 내부에서 값들을 꺼내야 함)
        LlmRequest llmRequest = new LlmRequest(
                new LlmRequest.Message(
                        "user",
                        req.message().userId(),  // 👈 수정: message 객체 안의 userId
                        req.message().content() // 👈 수정: message 객체 안의 content
                ));

        // 2️⃣ LLM 호출
        var llmResponse = llmClient.generate(llmRequest);

        // 3️⃣ userId 그대로 응답에 포함
        return new ChatResponseDto(
                req.message().userId(), // 👈 수정: 여기서도 message 객체 안의 userId 사용
                llmResponse.text(),
                llmResponse.model()
        );
    }
}