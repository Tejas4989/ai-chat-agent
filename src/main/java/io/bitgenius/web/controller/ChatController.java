package io.bitgenius.web.controller;

import io.bitgenius.domain.DraftExplainRequest;
import io.bitgenius.service.ChatService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam(defaultValue = "Hello") String question) {
        return chatService.simpleChat(question);
    }

    @PostMapping("/explain")
    public Map<String, Object> explain(@Valid @RequestBody DraftExplainRequest explainRequest) {
        return this.chatService.draftExplanation(explainRequest.topic(),
                explainRequest.tone(),
                explainRequest.audience());
    }
}
