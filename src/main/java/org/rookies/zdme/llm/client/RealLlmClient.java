package org.rookies.zdme.llm.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Profile("prod")
@Component
public class RealLlmClient implements LlmClient {

    private final String baseUrl;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final HttpClient httpClient;

    public RealLlmClient(@Value("${llm.base-url}") String baseUrl) {
        // base-url 끝에 슬래시가 있다면 제거하여 url 조합 시 이중 슬래시 방지
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public LlmResponse generate(LlmRequest request) {
        try {
            // 1. 전송할 데이터 Map 구성
            Map<String, Object> messageBody = new HashMap<>();
            messageBody.put("role", request.message().role());
            messageBody.put("user_id", request.message().userId());
            messageBody.put("content", request.message().content());

            Map<String, Object> payload = new HashMap<>();
            payload.put("message", messageBody);

            // 2. JSON 문자열 변환
            String jsonBody = objectMapper.writeValueAsString(payload);
            System.out.println("🔥 [Native HttpClient] 전송 JSON: " + jsonBody);

            // 3. 요청 생성 (curl 명령어를 그대로 코드로 옮긴 형태)
            // 주의: baseUrl + "/api/generate" 경로가 정확한지 확인
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/api/generate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                .build();

            // 4. 전송 및 응답 수신
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

            // 5. 응답 상태 코드 확인
            if (response.statusCode() != 200) {
                System.err.println("❌ 서버 응답 에러 코드: " + response.statusCode());
                System.err.println("❌ 서버 응답 본문: " + response.body());
                throw new RuntimeException("LLM 서버 에러: " + response.statusCode());
            }

            // 6. 응답 파싱
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