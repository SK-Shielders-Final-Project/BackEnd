package org.rookies.zdme.llm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration; // 시간 설정을 위해 추가
import java.util.HashMap;
import java.util.Map;

@Component
public class RealLlmClient implements LlmClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public RealLlmClient(@Value("${llm.base-url}") String baseUrl) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;

        // 1. HttpClient 생성 시 연결 타임아웃을 5분으로 설정
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofMinutes(5))
                .build();
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        try {
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("role", request.message().role());
            messageBody.put("user_id", request.message().userId());
            messageBody.put("content", request.message().content());

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", messageBody);

            String jsonBody = objectMapper.writeValueAsString(payload);
            System.out.println("🔥 [Native HttpClient] 전송 JSON: " + jsonBody);

            // 2. 개별 요청 시 응답을 기다리는 시간(timeout)을 5분으로 설정
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/api/generate"))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofMinutes(5))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("❌ 서버 응답 에러 코드: " + response.statusCode());
                System.err.println("❌ 서버 응답 본문: " + response.body());
                throw new RuntimeException("LLM 서버 에러: " + response.statusCode());
            }

            Map responseMap = objectMapper.readValue(response.body(), Map.class);
            String text = String.valueOf(responseMap.getOrDefault("text", ""));
            String model = String.valueOf(responseMap.getOrDefault("model", "unknown"));

            return new LlmResponse(text, model);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("LLM 호출 중 오류 발생", e);
        }
    }
}