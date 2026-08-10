package com.baseball.record.domain.record.dto;

import java.time.LocalDate;

/**
 * 티켓 이미지 OCR 파싱 결과.
 * 정규식이 못 찾은 필드는 null로 두고, requiresManualInput으로 클라이언트가
 * 나머지를 수동 입력 폼으로 채워야 하는지 판단하게 한다(구장/날짜는 다이어리 등록 필수값이므로
 * 둘 중 하나라도 비면 수동 입력이 필요하다고 본다).
 */
public record OcrResponse(
        String stadium,
        LocalDate matchDate,
        String section,
        String block,
        String rowNum,
        String seatNum,
        boolean requiresManualInput,
        String rawText
) {

    public static OcrResponse of(String stadium, LocalDate matchDate, String section, String block,
                                  String rowNum, String seatNum, String rawText) {
        boolean requiresManualInput = stadium == null || matchDate == null;
        return new OcrResponse(stadium, matchDate, section, block, rowNum, seatNum, requiresManualInput, rawText);
    }
}
