package com.example.crawler.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "htmlFetchClient", url = "${feign.htmlFetch.baseUrl:https://r.jina.ai/http}")
public interface HtmlFetchClient {
    
    // Simple passthrough proxy that returns fetched HTML using jina AI HTTP fetcher
    // This avoids cross-domain SSL complexities for demo and gives plain text body.
    @GetMapping("/")
    String fetch(@RequestParam("url") String targetUrl,
                 @RequestHeader(value = "User-Agent", required = false) String userAgent);
}


