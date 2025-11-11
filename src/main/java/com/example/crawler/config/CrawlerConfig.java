package com.example.crawler.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerConfig {
    
    @Value("${crawler.max.pages:100}")
    private int maxPages;
    
    @Value("${crawler.timeout:5000}")
    private int timeout;
    
    @Value("${crawler.user.agent:Mozilla/5.0}")
    private String userAgent;
    
    public int getMaxPages() { return maxPages; }
    public int getTimeout() { return timeout; }
    public String getUserAgent() { return userAgent; }
}


