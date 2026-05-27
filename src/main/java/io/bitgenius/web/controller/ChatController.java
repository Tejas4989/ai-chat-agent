package io.bitgenius.web.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;

@RestController
public class ChatController {

    private final ChatClient chatClient;

    public ChatController(ChatClient.Builder builder) {
        this.chatClient = builder
                .defaultSystem("""
                You are a concise assistant for a Canadian Insurance Broker.
                Always answer in under 3 sentences unless asked otherwise.
                """)
                .build();
    }


    /*@GetMapping("/chat")
    public String chat(@RequestParam(defaultValue = "Hello, how are you?") String question) {
        return this.chatClient.prompt().user(question).call().content();
    }*/

    @GetMapping("/chat")
    public Map<String, Object> chat(@RequestParam(defaultValue = "Hello") String question) {
        var response = chatClient.prompt()
                .user(question)
                .call()
                .chatResponse();

        assert response != null;
        var usage = response.getMetadata().getUsage();
        return Map.of(
                "answer", Objects.requireNonNull(response.getResult().getOutput().getText()),
                "tokens", Map.of(
                        "prompt", usage.getPromptTokens(),
                        "completion", usage.getCompletionTokens(),
                        "total", usage.getTotalTokens()
                ),
                "model", response.getMetadata().getModel()
        );
    }
}
