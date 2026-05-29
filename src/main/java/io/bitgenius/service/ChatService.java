package io.bitgenius.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

@Service
public class ChatService {

    private final ChatClient chatClient;

    // Spring injects the .st file as a Resource — zero parsing code needed
    @Value("classpath:prompts/insurance-assistant.st")
    private Resource insuranceAssistantResource;

    public ChatService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
        // Note: NOT calling .defaultSystem() here anymore.
        // We inject system dynamically per-request so tone/audience can vary.
    }

    /**
     * Simple chat — used by the existing /chat endpoint.
     * No template variables, just direct user input.
     */
    public Map<String, Object> simpleChat(String question) {
        var response = this.chatClient.prompt()
                .system("You are concise insurance assistant for Ontario, Canada")
                .user(question)
                .call()
                .chatResponse();

        assert response != null;
        return buildResponseMap(response);
    }

    /**
     * Templated chat — injects tone and audience into system prompt,
     * topic into user prompt. This is the Day 2 money shot.
     */
    public Map<String, Object> draftExplanation(String topic, String tone, String audience) {
        // prepare system prompt with runtime variable
        var systemPrompt = new PromptTemplate(insuranceAssistantResource)
                .render(Map.of("tone", tone, "audience", audience));

        var response = this.chatClient.prompt()
                .system(systemPrompt)
                .user(u ->
                        u.text("Explain the following insurance topic clearly: {topic}")
                                .param("topic", topic))
                .call()
                .chatResponse();

        assert response != null;
        return buildResponseMap(response);
    }

    private Map<String, Object> buildResponseMap(ChatResponse response) {
        var usage = response.getMetadata().getUsage();
        return Map.of("answer", Objects.requireNonNull(response.getResult().getOutput().getText()),
                "model", response.getMetadata().getModel(),
                "tokens", Map.of(
                        "prompt", usage.getPromptTokens(),
                        "completion", usage.getCompletionTokens(),
                        "total", usage.getTotalTokens())
        );

    }


}
