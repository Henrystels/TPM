package com.example.crawler.service;

import com.example.crawler.model.Company;
import com.example.crawler.model.CrawlTask;
import com.example.crawler.repository.CompanyRepository;
import com.example.crawler.util.ContactExtractor;
import com.example.crawler.util.HtmlParser;
import com.example.crawler.util.TracingUtil;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    
    // Metrics
    private final Timer parsingTimer;
    private final Timer htmlFetchTimer;
    private final Timer databaseSaveTimer;
    private final Counter parsingSuccessCounter;
    private final Counter parsingErrorCounter;
    private final Counter databaseRecordsCounter;
    private final Counter pagesCrawledCounter;
    private final Counter urlsVisitedCounter;
    private final TracingUtil tracingUtil;
    
    private final Map<String, CrawlTask> activeTasks = new ConcurrentHashMap<>();
    private final Set<String> visitedUrls = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final AtomicInteger totalPagesCrawled = new AtomicInteger(0);
    
    public CrawlerService(@Qualifier("crawlerExecutor") ExecutorService crawlerExecutor,
                         CompanyRepository companyRepository,
                         ContactExtractor contactExtractor,
                         HtmlParser htmlParser,
                         WebClientService webClientService,
                         RestTemplateService restTemplateService,
                         com.example.crawler.client.HtmlFetchClient htmlFetchClient,
                         Timer parsingTimer,
                         Timer htmlFetchTimer,
                         Timer databaseSaveTimer,
                         Counter parsingSuccessCounter,
                         Counter parsingErrorCounter,
                         Counter databaseRecordsCounter,
                         Counter pagesCrawledCounter,
                         Counter urlsVisitedCounter,
                         TracingUtil tracingUtil) {
        this.crawlerExecutor = crawlerExecutor;
        this.companyRepository = companyRepository;
        this.contactExtractor = contactExtractor;
        this.htmlParser = htmlParser;
        this.webClientService = webClientService;
        this.restTemplateService = restTemplateService;
        this.htmlFetchClient = htmlFetchClient;
        this.parsingTimer = parsingTimer;
        this.htmlFetchTimer = htmlFetchTimer;
        this.databaseSaveTimer = databaseSaveTimer;
        this.parsingSuccessCounter = parsingSuccessCounter;
        this.parsingErrorCounter = parsingErrorCounter;
        this.databaseRecordsCounter = databaseRecordsCounter;
        this.pagesCrawledCounter = pagesCrawledCounter;
        this.urlsVisitedCounter = urlsVisitedCounter;
        this.tracingUtil = tracingUtil;
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
            pagesCrawledCounter.increment();
            urlsVisitedCounter.increment();
            
            try {
                logger.info("Crawling: {}", currentUrl);
                
                // Trace HTML fetch
                String htmlContent = tracingUtil.trace("fetch_html", () -> 
                    htmlFetchTimer.recordCallable(() -> {
                        String content = webClientService.fetchHtmlContent(currentUrl).block();
                        if (content == null || content.isEmpty()) {
                            content = restTemplateService.fetchHtmlContent(currentUrl);
                        }
                        if (content == null || content.isEmpty()) {
                            try {
                                content = htmlFetchClient.fetch(currentUrl, "CompanyCrawler/1.0");
                            } catch (Exception ignore) {}
                        }
                        if (content == null || content.isEmpty()) {
                            content = htmlParser.fetchHtmlContent(currentUrl);
                        }
                        return content;
                    })
                );
                
                // Trace parsing
                Company company = tracingUtil.trace("parse_contacts", () -> 
                    parsingTimer.recordCallable(() -> 
                        contactExtractor.extractContacts(htmlContent, currentUrl)
                    )
                );
                
                if (company != null) {
                    parsingSuccessCounter.increment();
                    tracingUtil.trace("save_company", () -> saveCompanyIfNew(company));
                } else {
                    parsingErrorCounter.increment();
                }
                
                // Trace link extraction
                List<String> newUrls = tracingUtil.trace("extract_links", () -> 
                    htmlParser.extractLinks(htmlContent, currentUrl)
                );
                
                for (String newUrl : newUrls) {
                    if (!visitedUrls.contains(newUrl)) {
                        urlQueue.add(newUrl);
                    }
                }
                
                Thread.sleep(1000);
                
            } catch (Exception e) {
                parsingErrorCounter.increment();
                logger.warn("Failed to crawl URL: {}", currentUrl, e);
            }
        }
    }
    
    @Transactional
    private void saveCompanyIfNew(Company company) {
        databaseSaveTimer.record(() -> {
            Optional<Company> existing = companyRepository.findByWebsite(company.getWebsite());
            if (existing.isEmpty()) {
                companyRepository.save(company);
                databaseRecordsCounter.increment();
                logger.info("Saved company: {}", company.getName());
            }
        });
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


