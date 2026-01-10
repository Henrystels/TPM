package com.example.crawler.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.core.instrument.Counter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {
    
    @Bean
    public Timer parsingTimer(MeterRegistry registry) {
        return Timer.builder("crawler.parsing.duration")
                .description("Time taken to parse HTML content")
                .register(registry);
    }
    
    @Bean
    public Counter parsingSuccessCounter(MeterRegistry registry) {
        return Counter.builder("crawler.parsing.success")
                .description("Number of successful parsing operations")
                .register(registry);
    }
    
    @Bean
    public Counter parsingErrorCounter(MeterRegistry registry) {
        return Counter.builder("crawler.parsing.errors")
                .description("Number of failed parsing operations")
                .register(registry);
    }
    
    @Bean
    public Counter databaseRecordsCounter(MeterRegistry registry) {
        return Counter.builder("crawler.database.records.inserted")
                .description("Number of records inserted into database")
                .register(registry);
    }
    
    @Bean
    public Counter pagesCrawledCounter(MeterRegistry registry) {
        return Counter.builder("crawler.pages.crawled")
                .description("Total number of pages crawled")
                .register(registry);
    }
    
    @Bean
    public Counter urlsVisitedCounter(MeterRegistry registry) {
        return Counter.builder("crawler.urls.visited")
                .description("Total number of URLs visited")
                .register(registry);
    }
    
    @Bean
    public Timer databaseSaveTimer(MeterRegistry registry) {
        return Timer.builder("crawler.database.save.duration")
                .description("Time taken to save company to database")
                .register(registry);
    }
    
    @Bean
    public Timer htmlFetchTimer(MeterRegistry registry) {
        return Timer.builder("crawler.html.fetch.duration")
                .description("Time taken to fetch HTML content")
                .register(registry);
    }
}
