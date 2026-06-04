package io.bitgenius.domain.records;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import java.util.List;

/**
 * Extracted from natural language client description. All fields are nullable — extraction may be
 * partial if input is vague.
 */
@JsonPropertyOrder({
    "fullName",
    "businessName",
    "businessType",
    "coverageTypes",
    "estimatedRevenue",
    "numberOfEmployees",
    "city",
    "province",
    "riskScore",
    "extractionNotes"
})
public record InsuranceLead(
        @JsonPropertyDescription("Full name of the client or contact person") String fullName,
        @JsonPropertyDescription(
                        "Legal business name if commercial client, null for personal lines")
                String businessName,
        @JsonPropertyDescription(
                        """
                        Type of business, e.g. 'restaurant', 'retail', 'contractor', 'home-based salon'.
                        Null for personal lines clients.
                        """)
                String businessType,
        @JsonPropertyDescription(
                        """
                        List of insurance coverage types the client needs or mentioned.
                        Use standard short codes: CGL, COMMERCIAL_AUTO, PROPERTY, PROFESSIONAL_LIABILITY,
                        HOME, AUTO, TENANT, TRAVEL, LIFE. Include all that apply.
                        """)
                List<String> coverageTypes,
        @JsonPropertyDescription(
                        """
                        Estimated annual revenue in CAD as a number, no currency symbol.
                        Null if not mentioned or not a commercial client.
                        """)
                Long estimatedRevenue,
        @JsonPropertyDescription("Number of full-time equivalent employees. Null if not mentioned.")
                Integer numberOfEmployees,
        @JsonPropertyDescription("City of the business or client residence") String city,
        @JsonPropertyDescription("Canadian province abbreviation: ON, BC, AB, QC, etc.")
                String province,
        @JsonPropertyDescription(
                        """
                        Risk score integer 1-10 where 10 = highest risk.
                        Base this on: business type hazard level, revenue size, number of employees,
                        coverage types requested. Personal lines default to 3.
                        """)
                Integer riskScore,
        @JsonPropertyDescription(
                        """
                        Any important details from the description that don't fit other fields.
                        E.g. 'Client mentioned recent claim in 2024', 'Operates food trucks in addition to restaurant'.
                        Null if nothing notable.
                        """)
                String extractionNotes) {}
