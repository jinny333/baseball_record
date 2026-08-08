package com.baseball.record.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import kr.co.shineware.nlp.komoran.model.KomoranResult;
import org.junit.jupiter.api.Test;

/**
 * Spring 컨텍스트 없이 KOMORAN 모델 로딩과 커스텀 사전 인식이 실제로 동작하는지 확인하는 스모크 테스트.
 */
class KomoranSmokeTest {

    @Test
    void 커스텀_사전_단어를_고유명사로_인식한다() throws IOException {
        var komoran = new KomoranConfig().komoran();

        KomoranResult result = komoran.analyze("무적엘지 오지환 선수의 끝내기 안타");

        assertThat(result.getNouns()).contains("무적엘지", "오지환", "끝내기");
    }
}
