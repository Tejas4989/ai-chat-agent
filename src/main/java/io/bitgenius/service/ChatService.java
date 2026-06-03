package io.bitgenius.service;

import java.util.Map;
import java.util.Objects;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
     * Simple chat — used by the existing /chat endpoint. No template variables, just direct user
     * input.
     */
    public Map<String, Object> simpleChat(String question) {
        var response =
                this.chatClient
                        .prompt()
                        .system("You are concise insurance assistant for Ontario, Canada")
                        .user(question)
                        .call()
                        .chatResponse();

        assert response != null;
        return buildResponseMap(response);
    }

    /**
     * Templated chat — injects tone and audience into system prompt, topic into user prompt. This
     * is the Day 2 money shot.
     */
    public Map<String, Object> draftExplanation(String topic, String tone, String audience) {
        // prepare system prompt with runtime variable
        var systemPrompt =
                new PromptTemplate(insuranceAssistantResource)
                        .render(Map.of("tone", tone, "audience", audience));

        var response =
                this.chatClient
                        .prompt()
                        .system(systemPrompt)
                        .user(
                                u ->
                                        u.text(
                                                        "Explain the following insurance topic"
                                                                + " clearly: {topic}")
                                                .param("topic", topic))
                        .call()
                        .chatResponse();

        assert response != null;
        return buildResponseMap(response);
    }

    /*Day 3*/
    /**
     * Same prompt as draftExplanation() but returns tokens as they arrive. Returns Flux<String> —
     * each item is a small chunk of text (1-5 words typically). The caller (controller) pipes this
     * directly to the HTTP response as SSE.
     */
    public Flux<String> streamDraftExplanation(String topic, String tone, String audience) {
        var systemPrompt =
                new PromptTemplate(insuranceAssistantResource)
                        .render(Map.of("tone", tone, "audience", audience));
        return this.chatClient
                .prompt()
                .system(systemPrompt)
                .user(
                        u ->
                                u.text("Explain the following insurance topic clearly: {topic}")
                                        .param("topic", topic))
                .stream()
                .content()
                // Log each chunk arriving (remove in production)
                .doOnNext(
                        chunk -> {
                            // Uncomment to see chunks in server console:
                            // System.out.print(chunk);
                        })
                // If OpenAI drops the connection mid-stream, log it
                .doOnError(err -> System.err.println("Stream error: " + err.getMessage()))
                // Send an error marker to the browser instead of silent death
                .onErrorReturn("\n\n[Stream interrupted — please try again]");
    }

    /**
     * Use this when you need to know when the stream finishes and get token counts. Each
     * ChatResponse chunk has: text fragment + usage metadata on the LAST chunk.
     */
    public Flux<Map<String, Object>> streamDraftWithMetadata(
            String topic, String tone, String audience) {
        var systemPrompt =
                new PromptTemplate(insuranceAssistantResource)
                        .render(Map.of("tone", tone, "audience", audience));

        var fluxResponse =
                this.chatClient
                        .prompt()
                        .system(systemPrompt)
                        .user(
                                u ->
                                        u.text(
                                                        "Explain the following insurance topic"
                                                                + " clearly: {topic}")
                                                .param("topic", topic))
                        .stream()
                        .chatResponse();
        return buildResponseMap(fluxResponse);
    }

    private Flux<Map<String, Object>> buildResponseMap(Flux<ChatResponse> fluxResponse) {
        return fluxResponse
                .filter(
                        chatResponse -> {
                            // Safety: skip completely empty chunks that carry neither text nor
                            // usage
                            var result = chatResponse.getResult();
                            var usage = chatResponse.getMetadata().getUsage();
                            boolean hasText =
                                    result.getOutput().getText() != null
                                            && !result.getOutput().getText().isEmpty();
                            boolean hasUsage = usage != null && usage.getTotalTokens() > 0;
                            return hasText || hasUsage; // drop pure noise chunks
                        })
                .map(
                        chatResponse -> {
                            var result = chatResponse.getResult();
                            var text = result.getOutput().getText();
                            var usage = chatResponse.getMetadata().getUsage();

                            boolean isDone =
                                    usage != null
                                            && usage.getTotalTokens() > 0
                                            && (text == null || text.isBlank());

                            if (isDone) {
                                return Map.<String, Object>of(
                                        "text", "",
                                        "done", true,
                                        "promptTokens", usage.getPromptTokens(),
                                        "completionTokens", usage.getCompletionTokens(),
                                        "totalTokens", usage.getTotalTokens());
                            }

                            return Map.<String, Object>of(
                                    "text", text != null ? text : "", "done", false);
                        });
    }

    private Map<String, Object> buildResponseMap(ChatResponse response) {
        var usage = response.getMetadata().getUsage();
        return Map.of(
                "answer",
                Objects.requireNonNull(response.getResult().getOutput().getText()),
                "model",
                response.getMetadata().getModel(),
                "tokens",
                Map.of(
                        "prompt", usage.getPromptTokens(),
                        "completion", usage.getCompletionTokens(),
                        "total", usage.getTotalTokens()));
    }
}
