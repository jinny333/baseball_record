package com.baseball.record.domain.record.ocr;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
record VisionAnnotateResponse(List<AnnotateImageResponse> responses) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AnnotateImageResponse(FullTextAnnotation fullTextAnnotation, VisionApiError error) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record FullTextAnnotation(String text) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record VisionApiError(Integer code, String message) {
    }
}
