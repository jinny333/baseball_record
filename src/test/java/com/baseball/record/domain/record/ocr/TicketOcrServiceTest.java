package com.baseball.record.domain.record.ocr;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.baseball.record.domain.record.dto.OcrRequest;
import com.baseball.record.domain.record.dto.OcrResponse;
import com.baseball.record.global.error.BusinessException;
import com.baseball.record.global.error.ErrorCode;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TicketOcrServiceTest {

    @Mock
    private VisionApiClient visionApiClient;

    @Mock
    private TicketTextParser ticketTextParser;

    @InjectMocks
    private TicketOcrService ticketOcrService;

    @Test
    void Vision_API로_추출한_원문을_파서에_넘겨_파싱_결과를_반환한다() {
        OcrRequest request = new OcrRequest("https://example.com/ticket.jpg");
        given(visionApiClient.detectText(request.imageUrl())).willReturn("잠실야구장 2026.08.10 구역 105 블록 4 열 12 번 7");
        OcrResponse expected = OcrResponse.of(
                "잠실야구장", LocalDate.of(2026, 8, 10), "105", "4", "12", "7", "raw");
        given(ticketTextParser.parse("잠실야구장 2026.08.10 구역 105 블록 4 열 12 번 7")).willReturn(expected);

        OcrResponse response = ticketOcrService.parseTicket(request);

        assertThat(response).isEqualTo(expected);
        verify(visionApiClient).detectText(request.imageUrl());
        verify(ticketTextParser).parse("잠실야구장 2026.08.10 구역 105 블록 4 열 12 번 7");
    }

    @Test
    void Vision_API_호출이_실패하면_예외가_그대로_전파된다() {
        OcrRequest request = new OcrRequest("https://example.com/ticket.jpg");
        given(visionApiClient.detectText(request.imageUrl()))
                .willThrow(new BusinessException(ErrorCode.OCR_PROVIDER_ERROR));

        assertThatThrownBy(() -> ticketOcrService.parseTicket(request))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ErrorCode.OCR_PROVIDER_ERROR);
    }
}
