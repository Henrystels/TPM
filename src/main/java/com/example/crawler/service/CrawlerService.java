package com.example.crawler.service;

import com.example.crawler.model.Company;
import com.example.crawler.model.CrawlTask;
import com.example.crawler.repository.CompanyRepository;
import com.example.crawler.util.ContactExtractor;
import com.example.crawler.util.HtmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CrawlerService {
    
    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);
    
    private final ExecutorService crawlerExecutor;
    private final CompanyRepository companyRepository;
    private final ContactExtractor contactExtractor;
    private final HtmlParser htmlParser;
    private final WebClientService webClientService;
    private final RestTemplateService restTemplateService;
    private final com.example.crawler.client.HtmlFetchClient htmlFetchClient;
    
    private final Map<String, CrawlTask> activeTasks = new ConcurrentHashMap<>();
    private final Set<String> visitedUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicInteger totalPagesCrawled = new AtomicInteger(0);
    
    public CrawlerService(@Qualifier("crawlerExecutor") ExecutorService crawlerExecutor,
                         CompanyRepository companyRepository,
                         ContactExtractor contactExtractor,
                         HtmlParser htmlParser,
                         WebClientService webClientService,
                         RestTemplateService restTemplateService,
                         com.example.crawler.client.HtmlFetchClient htmlFetchClient) {
        this.crawlerExecutor = crawlerExecutor;
        this.companyRepository = companyRepository;
        this.contactExtractor = contactExtractor;
        this.htmlParser = htmlParser;
        this.webClientService = webClientService;
        this.restTemplateService = restTemplateService;
        this.htmlFetchClient = htmlFetchClient;
    }
    
    public String startCrawling(List<String> startUrls) {
        String taskId = UUID.randomUUID().toString();

        // Create and register the task BEFORE submitting, to avoid NPE race
        CrawlTask task = new CrawlTask(startUrls.toString());
        task.setStatus(CrawlTask.CrawlStatus.PENDING);
        activeTasks.put(taskId, task);

        Future<?> future = crawlerExecutor.submit(() -> {
            task.setStatus(CrawlTask.CrawlStatus.RUNNING);
            try {
                crawlWebsites(startUrls, task);
                task.setStatus(CrawlTask.CrawlStatus.COMPLETED);
                logger.info("Crawling completed for task {}. Total pages: {}", taskId, task.getPagesCrawled());
            } catch (Exception e) {
                task.setStatus(CrawlTask.CrawlStatus.FAILED);
                logger.error("Crawling failed for task {}", taskId, e);
            } finally {
                activeTasks.put(taskId, task);
            }
        });

        task.setFuture(future);
        return taskId;
    }
    
    private void crawlWebsites(List<String> urls, CrawlTask task) {
        Queue<String> urlQueue = new LinkedList<>(urls);
        
        while (!urlQueue.isEmpty() && task.getPagesCrawled() < 100) {
            String currentUrl = urlQueue.poll();
            
            if (visitedUrls.contains(currentUrl)) {
                continue;
            }
            
            visitedUrls.add(currentUrl);
            task.incrementPagesCrawled();
            totalPagesCrawled.incrementAndGet();
            
            try {
                logger.info("Crawling: {}", currentUrl);
                
                String htmlContent = webClientService.fetchHtmlContent(currentUrl).block();
                if (htmlContent == null || htmlContent.isEmpty()) {
                    htmlContent = restTemplateService.fetchHtmlContent(currentUrl);
                }
                if (htmlContent == null || htmlContent.isEmpty()) {
                    try {
                        htmlContent = htmlFetchClient.fetch(currentUrl, "CompanyCrawler/1.0");
                    } catch (Exception ignore) {}
                }
                if (htmlContent == null || htmlContent.isEmpty()) {
                    htmlContent = htmlParser.fetchHtmlContent(currentUrl);
                }
                
                Company company = contactExtractor.extractContacts(htmlContent, currentUrl);
                if (company != null) {
                    saveCompanyIfNew(company);
                }
                
                List<String> newUrls = htmlParser.extractLinks(htmlContent, currentUrl);
                for (String newUrl : newUrls) {
                    if (!visitedUrls.contains(newUrl)) {
                        urlQueue.add(newUrl);
                    }
                }
                
                Thread.sleep(1000);
                
            } catch (Exception e) {
                logger.warn("Failed to crawl URL: {}", currentUrl, e);
            }
        }
    }
    
    private void saveCompanyIfNew(Company company) {
        Optional<Company> existing = companyRepository.findByWebsite(company.getWebsite());
        if (existing.isEmpty()) {
            companyRepository.save(company);
            logger.info("Saved company: {}", company.getName());
        }
    }
    
    public CrawlTask getTaskStatus(String taskId) {
        return activeTasks.get(taskId);
    }
    
    public Map<String, CrawlTask> getActiveTasks() {
        return new HashMap<>(activeTasks);
    }
    
    public int getTotalPagesCrawled() {
        return totalPagesCrawled.get();
    }
}


