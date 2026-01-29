package org.rookies.zdme.chat.controller;

import jakarta.validation.Valid;
import org.rookies.zdme.chat.dto.ChatRequestDto;
import org.rookies.zdme.chat.dto.ChatResponseDto;
import org.rookies.zdme.chat.service.ChatService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/chat")
    public ChatResponseDto chat(@Valid @RequestBody ChatRequestDto req) {
        return chatService.chat(req);
    }

}
