package com.example.crawler.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class SchedulerService {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    
    private final CrawlerService crawlerService;
    private final ScheduledExecutorService scheduler;
    
    private static final List<String> DEFAULT_SITES = Arrays.asList(
        "https://www.example-business.com",
        "https://www.sample-company.org"
    );
    
    public SchedulerService(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
        this.scheduler = Executors.newScheduledThreadPool(1);
        
        startScheduledCrawling();
    }
    
    private void startScheduledCrawling() {
        scheduler.scheduleAtFixedRate(this::performScheduledCrawl, 0, 30, TimeUnit.MINUTES);
    }
    
    @Scheduled(cron = "0 0 2 * * ?")
    public void performDailyCrawl() {
        logger.info("Starting daily scheduled crawl");
        String taskId = crawlerService.startCrawling(DEFAULT_SITES);
        logger.info("Daily crawl task started: {}", taskId);
    }
    
    private void performScheduledCrawl() {
        logger.info("Starting scheduled crawl");
        String taskId = crawlerService.startCrawling(DEFAULT_SITES);
        logger.info("Scheduled crawl task started: {}", taskId);
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}


