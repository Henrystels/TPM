package com.example.crawler.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class RestTemplateService {
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    public String fetchHtmlContent(String url) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.add("User-Agent", "CompanyCrawler/1.0");
            HttpEntity<Void> request = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);
            return response.getBody();
        } catch (Exception e) {
            return null;
        }
    }
}


