package com.example.crawler.util;

import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class HtmlParser {
    
    private static final Pattern LINK_PATTERN = Pattern.compile(
        "<a[^>]+href=[\"']([^\"']+)[\"'][^>]*>", 
        Pattern.CASE_INSENSITIVE
    );
    
    public String fetchHtmlContent(String urlString) {
        StringBuilder content = new StringBuilder();
        
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            connection.setRequestProperty("User-Agent", "CompanyCrawler/1.0");
            
            int status = connection.getResponseCode();
            
            if (status == HttpURLConnection.HTTP_OK) {
                try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream()))) {
                    
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
            }
            
            connection.disconnect();
            
        } catch (Exception e) {
        }
        
        return content.toString();
    }
    
    public List<String> extractLinks(String htmlContent, String baseUrl) {
        List<String> links = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(htmlContent);
        
        try {
            URL base = new URL(baseUrl);
            
            while (matcher.find()) {
                String link = matcher.group(1);
                
                if (link.startsWith("/")) {
                    link = base.getProtocol() + "://" + base.getHost() + link;
                } else if (link.startsWith("./")) {
                    link = base.getProtocol() + "://" + base.getHost() + link.substring(1);
                } else if (!link.startsWith("http")) {
                    link = base.getProtocol() + "://" + base.getHost() + "/" + link;
                }
                
                if (link.startsWith("http") && !links.contains(link)) {
                    links.add(link);
                }
            }
            
        } catch (Exception e) {
        }
        
        return links;
    }
}


