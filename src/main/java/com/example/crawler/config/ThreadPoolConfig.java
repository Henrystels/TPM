package com.example.crawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

@Configuration
public class ThreadPoolConfig {
    
    @Bean("crawlerExecutor")
    public ExecutorService crawlerExecutor() {
        return Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("crawler-thread-" + t.getId());
            return t;
        });
    }
    
    @Bean("processingExecutor")
    public ForkJoinPool processingExecutor() {
        return new ForkJoinPool(Runtime.getRuntime().availableProcessors());
    }
}


