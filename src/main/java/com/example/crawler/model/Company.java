package com.example.crawler.model;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_website", columnList = "website"),
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_crawled_at", columnList = "crawled_at")
})
public class Company {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 500)
    private String name;
    
    @Column(length = 500, unique = true)
    private String website;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "company_phones", indexes = @Index(name = "idx_phone", columnList = "phone"))
    @Column(name = "phone")
    private List<String> phones;
    
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "company_emails", indexes = @Index(name = "idx_email", columnList = "email"))
    @Column(name = "email")
    private List<String> emails;
    
    @Column(length = 1000)
    private String address;
    
    @Column(length = 2000)
    private String description;
    
    @Column(name = "crawled_at")
    private LocalDateTime crawledAt;
    
    @Column(name = "source_url", length = 1000)
    private String sourceUrl;
    
    public Company() {}
    
    public Company(String name, String website, List<String> phones, 
                  List<String> emails, String address, String sourceUrl) {
        this.name = name;
        this.website = website;
        this.phones = phones;
        this.emails = emails;
        this.address = address;
        this.sourceUrl = sourceUrl;
        this.crawledAt = LocalDateTime.now();
    }
    
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public List<String> getPhones() { return phones; }
    public void setPhones(List<String> phones) { this.phones = phones; }
    public List<String> getEmails() { return emails; }
    public void setEmails(List<String> emails) { this.emails = emails; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCrawledAt() { return crawledAt; }
    public void setCrawledAt(LocalDateTime crawledAt) { this.crawledAt = crawledAt; }
    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }
}


