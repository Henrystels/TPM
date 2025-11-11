package com.example.crawler.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class WebClientService {
    
    private final WebClient webClient;
    
    public WebClientService() {
        this.webClient = WebClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "CompanyCrawler/1.0")
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.TEXT_HTML_VALUE)
            .build();
    }
    
    public Mono<String> fetchHtmlContent(String url) {
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(String.class)
            .onErrorResume(e -> Mono.empty());
    }
}


