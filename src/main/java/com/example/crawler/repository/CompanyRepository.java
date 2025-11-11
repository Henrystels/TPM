package com.example.crawler.repository;

import com.example.crawler.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByWebsite(String website);
    
    List<Company> findByNameContainingIgnoreCase(String name);
    
    @Query("SELECT c FROM Company c WHERE SIZE(c.phones) > 0 OR SIZE(c.emails) > 0")
    List<Company> findCompaniesWithContacts();
    
    List<Company> findByPhonesContaining(String phone);
    
    List<Company> findByEmailsContaining(String email);
}


