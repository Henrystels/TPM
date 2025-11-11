package com.example.crawler.model;

import java.util.concurrent.Future;

public class CrawlTask {
    private String url;
    private Future<?> future;
    private CrawlStatus status;
    private int pagesCrawled;
    
    public enum CrawlStatus {
        PENDING, RUNNING, COMPLETED, FAILED
    }
    
    public CrawlTask(String url) {
        this.url = url;
        this.status = CrawlStatus.PENDING;
        this.pagesCrawled = 0;
    }
    
    public String getUrl() { return url; }
    public Future<?> getFuture() { return future; }
    public void setFuture(Future<?> future) { this.future = future; }
    public CrawlStatus getStatus() { return status; }
    public void setStatus(CrawlStatus status) { this.status = status; }
    public int getPagesCrawled() { return pagesCrawled; }
    public void setPagesCrawled(int pagesCrawled) { this.pagesCrawled = pagesCrawled; }
    public void incrementPagesCrawled() { this.pagesCrawled++; }
}


