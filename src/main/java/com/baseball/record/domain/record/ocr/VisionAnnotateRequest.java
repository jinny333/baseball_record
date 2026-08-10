package com.baseball.record.domain.record.ocr;

import java.util.List;

record VisionAnnotateRequest(List<AnnotateImageRequest> requests) {

    record AnnotateImageRequest(Image image, List<Feature> features) {
    }

    record Image(ImageSource source) {
    }

    record ImageSource(String imageUri) {
    }

    record Feature(String type) {
    }

    static VisionAnnotateRequest of(String imageUrl) {
        return new VisionAnnotateRequest(List.of(
                new AnnotateImageRequest(
                        new Image(new ImageSource(imageUrl)),
                        List.of(new Feature("TEXT_DETECTION")))));
    }
}
