package org.rookies.zdme.llm.client;

import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
