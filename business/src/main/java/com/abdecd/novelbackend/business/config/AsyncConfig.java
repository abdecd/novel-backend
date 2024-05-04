package com.abdecd.novelbackend.business.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    /**
     * 仅针对IO密集的任务
     */
    @Override
    public Executor getAsyncExecutor() {
//        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(10, 10, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>(10000)));
        return Executors.newVirtualThreadPerTaskExecutor();
    }
}
