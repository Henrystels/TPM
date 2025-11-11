package com.example.crawler.util;

import com.example.crawler.model.Company;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ContactExtractor {
    
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "(\\+7|8)[\\s\\-\\(\\)]?\\d{3}[\\s\\-\\(\\)]?\\d{3}[\\s\\-\\(\\)]?\\d{2}[\\s\\-\\(\\)]?\\d{2}"
    );
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}"
    );
    
    private static final Pattern COMPANY_NAME_PATTERN = Pattern.compile(
        "<title>([^<]+)</title>", Pattern.CASE_INSENSITIVE
    );
    
    public Company extractContacts(String htmlContent, String sourceUrl) {
        if (htmlContent == null || htmlContent.trim().isEmpty()) {
            return null;
        }
        
        String companyName = extractCompanyName(htmlContent);
        List<String> phones = extractPhones(htmlContent);
        List<String> emails = extractEmails(htmlContent);
        String address = extractAddress(htmlContent);
        
        if (companyName == null || companyName.trim().isEmpty()) {
            companyName = "Unknown Company from " + sourceUrl;
        }
        
        String website = extractWebsite(sourceUrl);
        
        return new Company(companyName, website, phones, emails, address, sourceUrl);
    }
    
    private String extractCompanyName(String html) {
        Matcher matcher = COMPANY_NAME_PATTERN.matcher(html);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return null;
    }
    
    private List<String> extractPhones(String html) {
        List<String> phones = new ArrayList<>();
        Matcher matcher = PHONE_PATTERN.matcher(html);
        
        while (matcher.find()) {
            String phone = matcher.group().replaceAll("[\\s\\-\\(\\)]", "");
            if (!phones.contains(phone)) {
                phones.add(phone);
            }
        }
        
        return phones;
    }
    
    private List<String> extractEmails(String html) {
        List<String> emails = new ArrayList<>();
        Matcher matcher = EMAIL_PATTERN.matcher(html);
        
        while (matcher.find()) {
            String email = matcher.group().toLowerCase();
            if (!emails.contains(email)) {
                emails.add(email);
            }
        }
        
        return emails;
    }
    
    private String extractAddress(String html) {
        Pattern addressPattern = Pattern.compile("г\\.\\s*[А-Яа-я]+[,]?\\s*ул\\.\\s*[А-Яа-я]+[,]?\\s*д\\.\\s*\\d+");
        Matcher matcher = addressPattern.matcher(html);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }
    
    private String extractWebsite(String url) {
        try {
            java.net.URL uri = new java.net.URL(url);
            return uri.getProtocol() + "://" + uri.getHost();
        } catch (Exception e) {
            return url;
        }
    }
}


