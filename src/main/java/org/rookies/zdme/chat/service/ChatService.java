package org.rookies.zdme.chat.service;

import org.rookies.zdme.chat.dto.ChatRequestDto;
import org.rookies.zdme.chat.dto.ChatResponseDto;
import org.rookies.zdme.llm.client.LlmClient;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.springframework.stereotype.Service;

import java.util.List; // List 임포트 추가

@Service
public class ChatService {

    private final LlmClient llmClient;

    public ChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public ChatResponseDto chat(ChatRequestDto req) {

        // 1️⃣ 개별 메시지 객체 생성
        LlmRequest.Message message = new LlmRequest.Message(
                "user",
                req.message().userId(),
                req.message().content()
        );

        // 2️⃣ 리스트로 감싸서 LlmRequest 생성 (이 부분이 핵심입니다!)
        LlmRequest llmRequest = new LlmRequest(message);

        // 3️⃣ LLM 호출
        var llmResponse = llmClient.generate(llmRequest);

        // 4️⃣ 응답 반환
        return new ChatResponseDto(
                req.message().userId(),
                llmResponse.text(),
                llmResponse.model()
        );
    }
}