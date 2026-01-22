package org.rookies.zdme.llm.client;

import org.rookies.zdme.llm.dto.LlmRequest;
import org.rookies.zdme.llm.dto.LlmResponse;

public interface LlmClient {
    LlmResponse generate(LlmRequest request);
}
