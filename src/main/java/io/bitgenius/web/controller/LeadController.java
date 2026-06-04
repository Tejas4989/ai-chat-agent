package io.bitgenius.web.controller;

import io.bitgenius.domain.records.ExtractionDTO.BulkExtractRequest;
import io.bitgenius.domain.records.ExtractionDTO.ExtractRequest;
import io.bitgenius.domain.records.InsuranceLead;
import io.bitgenius.service.ChatService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
public class LeadController {

    private final ChatService chatService;

    @PostMapping("/extract")
    public InsuranceLead extractLead(@Valid @RequestBody ExtractRequest extractRequest) {
        return this.chatService.extractLead(extractRequest.description());
    }

    @PostMapping("/extract/bulk")
    public List<InsuranceLead> extractLeadBulk(
            @Valid @RequestBody BulkExtractRequest bulkExtractRequest) {
        return this.chatService.extractLeads(bulkExtractRequest.descriptions());
    }
}
