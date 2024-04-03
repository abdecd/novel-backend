package com.abdecd.novelbackend.business.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig {
    /**
     * 仅针对IO密集的任务
     */
    @Bean("taskExecutor")
    public Executor taskExecutor() {
//        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000)));
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
