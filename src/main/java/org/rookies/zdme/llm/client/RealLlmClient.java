package org.rookies.zdme.llm.client;

import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Profile("prod")
@Component
public class RealLlmClient implements LlmClient {

    private final RestClient restClient;

    public RealLlmClient(@Value("${llm.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse generate(LlmRequest request) {
        Map<String, Object> messageBody = new HashMap<>();
        // request에서 데이터를 꺼내서 직접 넣습니다.
        messageBody.put("role", request.message().role());
        messageBody.put("user_id", request.message().userId()); // "user_id" 키 이름 주의
        messageBody.put("content", request.message().content());

        Map<String, Object> payload = new HashMap<>();
        payload.put("message", messageBody);

        System.out.println("🚀 강제로 만든 Payload: " + payload);

        // ⚠️ 아래는 예시 스펙: POST /generate -> { "text": "...", "model": "..." }
        Map<String, Object> res = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(Map.class);

        String text = String.valueOf(res.getOrDefault("text", ""));
        String model = String.valueOf(res.getOrDefault("model", "unknown"));
        return new LlmResponse(text, model);
    }
}
