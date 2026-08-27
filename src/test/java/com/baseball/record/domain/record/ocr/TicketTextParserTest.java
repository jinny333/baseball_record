package com.baseball.record.domain.record.ocr;

import static org.assertj.core.api.Assertions.assertThat;

import com.baseball.record.domain.record.dto.OcrResponse;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class TicketTextParserTest {

    private final TicketTextParser ticketTextParser = new TicketTextParser();

    @Test
    void 라벨이_값_앞에_붙는_티켓링크_포맷을_파싱한다() {
        String rawText = """
                잠실야구장
                2026.08.10(월) 18:30
                구역 105 블록 4 열 12 번 7
                """;

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isEqualTo("잠실야구장");
        assertThat(response.matchDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.section()).isEqualTo("105");
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
        assertThat(response.requiresManualInput()).isFalse();
    }

    @Test
    void 라벨이_값_뒤에_붙는_인터파크_포맷을_파싱한다() {
        String rawText = "고척스카이돔 2026-08-10 1루 오렌지석 105구역 4블록 12열 7번";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isEqualTo("고척스카이돔");
        assertThat(response.matchDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.section()).isEqualTo("105");
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
    }

    @Test
    void 영문_라벨_포맷을_파싱한다() {
        String rawText = "창원NC파크\n2026/08/10\nSection: 105 Block: 4 Row: 12 Seat: 7";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isEqualTo("창원NC파크");
        assertThat(response.matchDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.section()).isEqualTo("105");
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
    }

    @Test
    void 구역_라벨이_없는_구단_자체_앱_포맷은_구역없이_파싱한다() {
        String rawText = "사직야구장 2026년 8월 10일 H2블록 5열 12번";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isEqualTo("사직야구장");
        assertThat(response.matchDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.section()).isNull();
        assertThat(response.block()).isEqualTo("H2");
        assertThat(response.rowNum()).isEqualTo("5");
        assertThat(response.seatNum()).isEqualTo("12");
    }

    @Test
    void 어떤_패턴과도_매칭되지_않으면_모든_필드가_비고_수동입력이_필요하다() {
        String rawText = "인식할 수 없는 임의의 텍스트입니다.";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isNull();
        assertThat(response.matchDate()).isNull();
        assertThat(response.section()).isNull();
        assertThat(response.block()).isNull();
        assertThat(response.rowNum()).isNull();
        assertThat(response.seatNum()).isNull();
        assertThat(response.requiresManualInput()).isTrue();
        assertThat(response.rawText()).isEqualTo(rawText);
    }

    @Test
    void 값과_라벨_사이에_공백이_있는_인터파크_포맷도_파싱한다() {
        String rawText = "고척스카이돔 2026-08-10 105 구역 4 블록 12 열 7 번";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.section()).isEqualTo("105");
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
    }

    @Test
    void 구역_라벨이_없는_영문_포맷을_파싱한다() {
        String rawText = "잠실야구장 2026.08.10 Block: 4 Row: 12 Seat: 7";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.section()).isNull();
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
    }

    @Test
    void 축약_영문_라벨과_쉼표_구분자_포맷을_파싱한다() {
        String rawText = "Sec: 105, Blk: 4, Row: 12, Seat: 7";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.section()).isEqualTo("105");
        assertThat(response.block()).isEqualTo("4");
        assertThat(response.rowNum()).isEqualTo("12");
        assertThat(response.seatNum()).isEqualTo("7");
    }

    @Test
    void 구장과_날짜만_인식되고_좌석은_실패해도_수동입력이_필요하지_않다() {
        String rawText = "잠실야구장 2026.08.10 좌석 정보 인식 실패";

        OcrResponse response = ticketTextParser.parse(rawText);

        assertThat(response.stadium()).isEqualTo("잠실야구장");
        assertThat(response.matchDate()).isEqualTo(LocalDate.of(2026, 8, 10));
        assertThat(response.section()).isNull();
        assertThat(response.block()).isNull();
        assertThat(response.requiresManualInput()).isFalse();
    }
}
