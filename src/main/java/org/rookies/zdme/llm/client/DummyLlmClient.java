package org.rookies.zdme.llm.client;

import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Profile("dev")
@Component
public class DummyLlmClient implements LlmClient {

    @Override
    public LlmResponse generate(LlmRequest request) {
        // 마지막 user 메시지를 찾아서 에코
        String lastUser = "";
        if (request != null && request.messages() != null) {
            for (int i = request.messages().size() - 1; i >= 0; i--) {
                var m = request.messages().get(i);
                if (m != null && "user".equalsIgnoreCase(m.role())) {
                    lastUser = m.content();
                    break;
                }
            }
        }

        String answer = "DUMMY ANSWER: " + (lastUser == null ? "" : lastUser);
        return new LlmResponse(answer, "dummy-llm");
    }
}
