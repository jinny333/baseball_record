package com.baseball.record.global.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 리뷰 저장 트랜잭션(요청 스레드)과 형태소 분석/추천 벡터 계산(백그라운드)을 완전히 분리하기 위한 전용 스레드풀.
 * 기본 SimpleAsyncTaskExecutor(무제한 스레드 생성)를 쓰지 않고 풀 크기를 제한해,
 * 리뷰 작성이 몰려도 서버 전체 스레드 자원을 잠식하지 않도록 한다.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("review-analysis-");
        executor.initialize();
        return executor;
    }
}
