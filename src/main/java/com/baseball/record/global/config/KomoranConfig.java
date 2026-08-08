package com.baseball.record.global.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import kr.co.shineware.nlp.komoran.constant.DEFAULT_MODEL;
import kr.co.shineware.nlp.komoran.core.Komoran;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Komoran은 형태소 분석 모델을 메모리에 적재하는 비용이 커서 요청마다 새로 생성하면 안 되고,
 * 애플리케이션 전역에서 단 하나의 인스턴스를 재사용해야 한다(스레드 세이프).
 * 커스텀 사전(user_dic.txt)은 jar 내부 classpath 리소스라 파일 경로를 직접 넘길 수 없으므로,
 * 기동 시 임시 파일로 풀어낸 뒤 그 경로를 Komoran에 등록한다.
 */
@Configuration
public class KomoranConfig {

    private static final String USER_DIC_CLASSPATH = "komoran/user_dic.txt";

    @Bean
    public Komoran komoran() throws IOException {
        Komoran komoran = new Komoran(DEFAULT_MODEL.FULL);
        komoran.setUserDic(extractUserDic().toString());
        return komoran;
    }

    private Path extractUserDic() throws IOException {
        Path tempFile = Files.createTempFile("komoran-user-dic-", ".txt");
        tempFile.toFile().deleteOnExit();
        try (InputStream in = new ClassPathResource(USER_DIC_CLASSPATH).getInputStream()) {
            Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFile;
    }
}
