package com.example.pdfref;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ReferenceItem(
        @JsonProperty("reference_file_name") String referenceFileName,
        @JsonProperty("file_code") String fileCode) {
}
