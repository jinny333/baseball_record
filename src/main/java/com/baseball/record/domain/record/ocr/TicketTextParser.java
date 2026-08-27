package com.baseball.record.domain.record.ocr;

import com.baseball.record.domain.record.dto.OcrResponse;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Vision API가 추출한 티켓 원문 텍스트에서 구장/날짜/좌석(구역·블록·열·번호)을 정규식으로 파싱한다.
 * 예매처마다 티켓 레이아웃이 달라 좌석 표기 순서·구분자가 제각각이므로,
 * 자주 보이는 포맷 순으로 정규식을 순차 매칭해 첫 성공 결과를 채택한다.
 * 전부 실패해도 예외를 던지지 않고 null 필드로 남겨, 컨트롤러 응답의 requiresManualInput으로
 * 클라이언트가 수동 입력 폼을 채우도록 유도한다(정규식 커버리지 확장은 MVP 이후 과제).
 */
@Component
public class TicketTextParser {

    private static final Map<String, String> STADIUM_ALIASES = Map.ofEntries(
            Map.entry("잠실", "잠실야구장"),
            Map.entry("고척", "고척스카이돔"),
            Map.entry("문학", "인천SSG랜더스필드"),
            Map.entry("랜더스필드", "인천SSG랜더스필드"),
            Map.entry("수원", "수원KT위즈파크"),
            Map.entry("대구", "대구삼성라이온즈파크"),
            Map.entry("라이온즈파크", "대구삼성라이온즈파크"),
            Map.entry("광주", "광주기아챔피언스필드"),
            Map.entry("챔피언스필드", "광주기아챔피언스필드"),
            Map.entry("대전", "대전한화생명이글스파크"),
            Map.entry("이글스파크", "대전한화생명이글스파크"),
            Map.entry("창원", "창원NC파크"),
            Map.entry("NC파크", "창원NC파크"),
            Map.entry("사직", "사직야구장"));

    // 날짜: 2026.08.10 / 2026-08-10 / 2026/08/10 / 2026년 8월 10일 등 구분자 무관하게 매칭.
    private static final Pattern DATE_PATTERN =
            Pattern.compile("(\\d{4})\\s*[.\\-/년]\\s*(\\d{1,2})\\s*[.\\-/월]\\s*(\\d{1,2})\\s*일?");

    // 포맷 A(티켓링크 등): "구역 105 블록 4 열 12 번 7" — 라벨이 값 앞에, 줄바꿈 포함 가능.
    private static final Pattern LABELED_SEAT_PATTERN = Pattern.compile(
            "구역\\s*[:：]?\\s*(\\S+?)\\s*블록\\s*[:：]?\\s*(\\S+?)\\s*열\\s*[:：]?\\s*(\\S+?)\\s*번\\s*[:：]?\\s*(\\S+)",
            Pattern.DOTALL);

    // 포맷 B(인터파크 등): "105구역 4블록 12열 7번" — 라벨이 값 뒤에 붙는 스타일.
    // 값과 라벨 사이에 공백이 있는 "105 구역 4 블록 12 열 7 번" 형태도 함께 허용한다.
    private static final Pattern SUFFIX_SEAT_PATTERN =
            Pattern.compile("(\\S+?)\\s*구역\\s*(\\S+?)\\s*블록\\s*(\\S+?)\\s*열\\s*(\\S+?)\\s*번");

    // 포맷 C(영문 라벨을 쓰는 예매처): "Section 105 Block 4 Row 12 Seat 7".
    // Sec/Blk 같은 축약 라벨과 쉼표로 구분된 "Section: 105, Block: 4, ..." 형태도 허용한다.
    private static final Pattern ENGLISH_SEAT_PATTERN = Pattern.compile(
            "(?:Section|Sec)\\.?\\s*[:：]?\\s*(\\S+?)[\\s,]*(?:Block|Blk)\\.?\\s*[:：]?\\s*(\\S+?)[\\s,]*"
                    + "Row\\s*[:：]?\\s*(\\S+?)[\\s,]*Seat\\s*[:：]?\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    // 포맷 D(구단 자체 앱 등): 구역 라벨 없이 "H2블록 5열 12번"만 있는 경우 — 구역은 비워둔다.
    private static final Pattern BLOCK_ONLY_PATTERN =
            Pattern.compile("(\\S+?)\\s*블록\\s*(\\S+?)\\s*열\\s*(\\S+?)\\s*번");

    // 포맷 E: 영문 라벨이지만 구역(Section) 없이 "Block 4 Row 12 Seat 7"만 있는 경우.
    private static final Pattern ENGLISH_BLOCK_ONLY_PATTERN = Pattern.compile(
            "(?:Block|Blk)\\.?\\s*[:：]?\\s*(\\S+?)[\\s,]*Row\\s*[:：]?\\s*(\\S+?)[\\s,]*Seat\\s*[:：]?\\s*(\\S+)",
            Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

    private static final List<Pattern> SEAT_PATTERNS_WITH_SECTION =
            List.of(LABELED_SEAT_PATTERN, SUFFIX_SEAT_PATTERN, ENGLISH_SEAT_PATTERN);

    private static final List<Pattern> SEAT_PATTERNS_WITHOUT_SECTION =
            List.of(BLOCK_ONLY_PATTERN, ENGLISH_BLOCK_ONLY_PATTERN);

    public OcrResponse parse(String rawText) {
        String text = rawText == null ? "" : rawText.trim();
        String stadium = parseStadium(text);
        LocalDate matchDate = parseDate(text);
        Seat seat = parseSeat(text);

        return OcrResponse.of(stadium, matchDate, seat.section(), seat.block(), seat.rowNum(), seat.seatNum(), text);
    }

    private String parseStadium(String text) {
        return STADIUM_ALIASES.entrySet().stream()
                .filter(entry -> text.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private LocalDate parseDate(String text) {
        Matcher matcher = DATE_PATTERN.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        try {
            return LocalDate.of(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private Seat parseSeat(String text) {
        for (Pattern pattern : SEAT_PATTERNS_WITH_SECTION) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return new Seat(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
            }
        }

        for (Pattern pattern : SEAT_PATTERNS_WITHOUT_SECTION) {
            Matcher matcher = pattern.matcher(text);
            if (matcher.find()) {
                return new Seat(null, matcher.group(1), matcher.group(2), matcher.group(3));
            }
        }

        return new Seat(null, null, null, null);
    }

    private record Seat(String section, String block, String rowNum, String seatNum) {
    }
}
