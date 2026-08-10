package com.baseball.record.domain.record.ocr;

import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

/**
 * Google Cloud Vision API(TEXT_DETECTION)로 티켓 이미지에서 원문 텍스트를 추출한다.
 * 이미지를 직접 업로드하지 않고 imageUri(공개 URL)로 요청하므로,
 * 별도의 파일 업로드 인프라 없이 기존 imageUrl 컬럼 값을 그대로 넘길 수 있다.
 */
@Component
public class VisionApiClient {

    private static final String ANNOTATE_URI = "https://vision.googleapis.com/v1/images:annotate?key={key}";

    private final RestClient restClient = RestClient.create();
    private final String apiKey;

    public VisionApiClient(@Value("${vision.api-key:}") String apiKey) {
        this.apiKey = apiKey;
    }

    public String detectText(String imageUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_PROVIDER_ERROR);
        }

        VisionAnnotateResponse response;
        try {
            response = restClient.post()
                    .uri(ANNOTATE_URI, apiKey)
                    .body(VisionAnnotateRequest.of(imageUrl))
                    .retrieve()
                    .body(VisionAnnotateResponse.class);
        } catch (RestClientException e) {
            throw new BusinessException(ErrorCode.OCR_PROVIDER_ERROR);
        }

        List<VisionAnnotateResponse.AnnotateImageResponse> results =
                response == null ? null : response.responses();
        if (results == null || results.isEmpty()) {
            throw new BusinessException(ErrorCode.OCR_PROVIDER_ERROR);
        }

        VisionAnnotateResponse.AnnotateImageResponse result = results.get(0);
        if (result.error() != null) {
            throw new BusinessException(ErrorCode.OCR_PROVIDER_ERROR);
        }

        String text = result.fullTextAnnotation() == null ? null : result.fullTextAnnotation().text();
        if (text == null || text.isBlank()) {
            throw new BusinessException(ErrorCode.OCR_TEXT_NOT_FOUND);
        }
        return text;
    }
}
