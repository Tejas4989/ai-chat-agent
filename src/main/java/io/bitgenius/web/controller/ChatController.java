package io.bitgenius.web.controller;

import io.bitgenius.domain.records.DraftExplainRequest;
import io.bitgenius.service.ChatService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

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
        return this.chatService.draftExplanation(
                explainRequest.topic(), explainRequest.tone(), explainRequest.audience());
    }

    // ---------------------------------------------------------------
    // Day 3: Streaming endpoint — tokens arrive one by one as SSE
    // produces = TEXT_EVENT_STREAM_VALUE is the magic line.
    // Without it Spring buffers everything and sends one big response.
    // ---------------------------------------------------------------
    @PostMapping(value = "/explain/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamExplain(@Valid @RequestBody DraftExplainRequest req) {
        return chatService.streamDraftExplanation(req.topic(), req.tone(), req.audience());
    }

    // ---------------------------------------------------------------
    // Day 3 bonus: stream where you can see token usage at the end
    // Each SSE event is JSON: {"text": "...", "done": false}
    // Last event: {"text": "", "done": true, "totalTokens": 143}
    // ---------------------------------------------------------------
    @PostMapping(
            value = "/explain/streamWithMetadata",
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Map<String, Object>> streamExplainWithMetadata(
            @Valid @RequestBody DraftExplainRequest req) {
        /*return chatService.streamDraftWithMetadata(req.topic(), req.tone(), req.audience())
        .map(chatResponse -> {
            var text = chatResponse.getResult().getOutput().getText();
            var usage = chatResponse.getMetadata().getUsage();
            boolean isDone = usage != null && usage.getTotalTokens() > 0;

            if (isDone) {
                // Last chunk — includes token counts
                return Map.<String, Object>of(
                        "text", text != null ? text : "",
                        "done", true,
                        "totalTokens", usage.getTotalTokens()
                );
            }
            return Map.<String, Object>of(
                    "text", text != null ? text : "",
                    "done", false
            );
        });*/
        return this.chatService.streamDraftWithMetadata(req.topic(), req.tone(), req.audience());
    }
}
