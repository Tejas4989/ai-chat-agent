package io.bitgenius.domain;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * tone: "professional" | "friendly" | "technical"
 * audience: "first-time buyer" | "business owner" | "claims adjuster"
 */
public record DraftExplainRequest(
        @NotBlank(message = "topic is required")
        String topic,

        @Pattern(
                regexp = "professional|friendly|technical",
                message = "tone must be professional, friendly, or technical"
        )
        String tone,

        @NotBlank(message = "audience is required")
        String audience
) {
}
