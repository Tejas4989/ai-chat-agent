package io.bitgenius.domain.records;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public class ExtractionDTO {

    private ExtractionDTO() {}

    public record ExtractRequest(
            @NotBlank(message = "description is required") String description) {}

    public record BulkExtractRequest(
            @NotEmpty(message = "at least one description required")
                    List<@NotBlank String> descriptions) {}
}
