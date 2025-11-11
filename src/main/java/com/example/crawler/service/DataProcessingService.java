package com.example.crawler.service;

import com.example.crawler.model.Company;
import com.example.crawler.repository.CompanyRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;

@Service
public class DataProcessingService {
    
    private final CompanyRepository companyRepository;
    private final ForkJoinPool processingExecutor;
    
    public DataProcessingService(CompanyRepository companyRepository,
                               @Qualifier("processingExecutor") ForkJoinPool processingExecutor) {
        this.companyRepository = companyRepository;
        this.processingExecutor = processingExecutor;
    }
    
    public List<Company> filterAndSortCompanies(String searchTerm, String sortBy, boolean ascending) {
        List<Company> allCompanies = companyRepository.findCompaniesWithContacts();
        
        return processingExecutor.submit(() -> 
            allCompanies.parallelStream()
                .filter(company -> matchesSearchTerm(company, searchTerm))
                .sorted(getComparator(sortBy, ascending))
                .collect(Collectors.toList())
        ).join();
    }
    
    private boolean matchesSearchTerm(Company company, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return true;
        }
        
        String term = searchTerm.toLowerCase();
        return (company.getName() != null && company.getName().toLowerCase().contains(term)) ||
               (company.getWebsite() != null && company.getWebsite().toLowerCase().contains(term)) ||
               (company.getAddress() != null && company.getAddress().toLowerCase().contains(term)) ||
               (company.getPhones() != null && company.getPhones().stream()
                   .anyMatch(phone -> phone.contains(term))) ||
               (company.getEmails() != null && company.getEmails().stream()
                   .anyMatch(email -> email.contains(term)));
    }
    
    private Comparator<Company> getComparator(String sortBy, boolean ascending) {
        Comparator<Company> comparator;
        
        switch (sortBy != null ? sortBy.toLowerCase() : "name") {
            case "website":
                comparator = Comparator.comparing(Company::getWebsite, 
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
                break;
            case "crawledat":
                comparator = Comparator.comparing(Company::getCrawledAt, 
                    Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "name":
            default:
                comparator = Comparator.comparing(Company::getName, 
                    Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        }
        
        return ascending ? comparator : comparator.reversed();
    }
    
    public List<Company> findCompaniesByPhone(String phone) {
        return processingExecutor.submit(() -> 
            companyRepository.findByPhonesContaining(phone).parallelStream()
                .collect(Collectors.toList())
        ).join();
    }
    
    public List<Company> findCompaniesByEmail(String email) {
        return processingExecutor.submit(() -> 
            companyRepository.findByEmailsContaining(email).parallelStream()
                .collect(Collectors.toList())
        ).join();
    }
}


