package com.example.crawler.controller;

import com.example.crawler.model.CrawlTask;
import com.example.crawler.service.CrawlerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {
    
    private final CrawlerService crawlerService;
    
    public CrawlerController(CrawlerService crawlerService) {
        this.crawlerService = crawlerService;
    }
    
    @PostMapping("/start")
    public ResponseEntity<Map<String, String>> startCrawling(@RequestBody List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "URL list cannot be empty"));
        }
        
        String taskId = crawlerService.startCrawling(urls);
        return ResponseEntity.ok(Map.of("taskId", taskId, "message", "Crawling started"));
    }
    
    @GetMapping("/status/{taskId}")
    public ResponseEntity<CrawlTask> getTaskStatus(@PathVariable String taskId) {
        CrawlTask task = crawlerService.getTaskStatus(taskId);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }
    
    @GetMapping("/active-tasks")
    public ResponseEntity<Map<String, CrawlTask>> getActiveTasks() {
        return ResponseEntity.ok(crawlerService.getActiveTasks());
    }
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = Map.of(
            "totalPagesCrawled", crawlerService.getTotalPagesCrawled(),
            "activeTasks", crawlerService.getActiveTasks().size()
        );
        return ResponseEntity.ok(stats);
    }
}


