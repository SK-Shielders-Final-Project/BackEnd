package org.rookies.zdme.chat.service;

import org.rookies.zdme.chat.dto.ChatHistoryItem;
import org.rookies.zdme.chat.dto.ChatRequestDto;
import org.rookies.zdme.chat.dto.ChatResponseDto;
import org.rookies.zdme.llm.client.LlmClient;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class ChatService {

    private final LlmClient llmClient;

    public ChatService(LlmClient llmClient) {
        this.llmClient = llmClient;
    }

    public ChatResponseDto chat(ChatRequestDto req) {
        String conversationId = normalizeConversationId(req.conversationId());

        // history null 방어 + 길이 제한(너무 길면 느려짐)
        List<ChatHistoryItem> history = (req.history() == null) ? List.of() : req.history();
        history = trimHistory(history, 10); // 최근 10개만 사용(원하면 조절)

        LlmRequest llmRequest = toLlmRequest(history, req.message());
        LlmResponse llmResponse = llmClient.generate(llmRequest);

        return new ChatResponseDto(
                conversationId,
                llmResponse.text(),
                llmResponse.model()
        );
    }

    private String normalizeConversationId(String conversationId) {
        if (conversationId == null || conversationId.isBlank()) {
            return "c_" + UUID.randomUUID();
        }
        return conversationId;
    }

    private List<ChatHistoryItem> trimHistory(List<ChatHistoryItem> history, int maxItems) {
        if (history.size() <= maxItems) return history;
        return history.subList(history.size() - maxItems, history.size());
    }

    private LlmRequest toLlmRequest(List<ChatHistoryItem> history, String message) {
        List<LlmRequest.Message> messages = new ArrayList<>();

        // 시스템 프롬프트(원하는 정책/캐릭터 있으면 여기서 관리)
        messages.add(new LlmRequest.Message(
                "system",
                "당신은 한국어로 친절하고 간결하게 답변하는 챗봇입니다."
        ));

        for (ChatHistoryItem h : history) {
            if (h == null) continue;
            String role = safeRole(h.role());
            String content = (h.content() == null) ? "" : h.content();
            messages.add(new LlmRequest.Message(role, content));
        }

        messages.add(new LlmRequest.Message("user", message));
        return new LlmRequest(messages);
    }

    private String safeRole(String role) {
        if (role == null) return "user";
        String r = role.trim().toLowerCase();
        // 허용 role만 통과
        if (r.equals("user") || r.equals("assistant") || r.equals("system")) return r;
        return "user";
    }
}
