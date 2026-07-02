package com.example.pdfref;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record FileReferenceResult(
        @JsonProperty("file_name") String fileName,
        @JsonProperty("references") List<ReferenceItem> references) {
}
