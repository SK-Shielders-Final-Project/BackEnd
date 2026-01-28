package org.rookies.zdme.llm.client;

import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Profile("prod")
@Component
public class RealLlmClient implements LlmClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RealLlmClient(@Value("${llm.base-url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    @SuppressWarnings("unchecked")
    public LlmResponse generate(LlmRequest request){
        try {
            Map<String, Object> messageBody = new HashMap<>();
            // request에서 데이터를 꺼내서 직접 넣습니다.
            messageBody.put("role", request.message().role());
            messageBody.put("user_id", request.message().userId()); // "user_id" 키 이름 주의
            messageBody.put("content", request.message().content());

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", messageBody);

            String jsonBody = objectMapper.writeValueAsString(payload);
            System.out.println("🚀 전송할 JSON: " + jsonBody);

            byte[] bodyBytes = jsonBody.getBytes(StandardCharsets.UTF_8);
            // ⚠️ 아래는 예시 스펙: POST /generate -> { "text": "...", "model": "..." }
            Map<String, Object> res = restClient.post()
                .uri("/api/generate")
                .contentType(MediaType.APPLICATION_JSON)
                .body(bodyBytes)
                .retrieve()
                .body(Map.class);

            if (res == null) {
                return new LlmResponse("", "unknown");
            }

            String text = String.valueOf(res.getOrDefault("text", ""));
            String model = String.valueOf(res.getOrDefault("model", "unknown"));
            return new LlmResponse(text, model);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패", e);
        } catch (Exception e) {
            // 에러 발생 시 로그를 남기고 예외를 다시 던짐
            System.err.println("❌ 에러 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("LLM 호출 오류", e);
        }

    }
}
