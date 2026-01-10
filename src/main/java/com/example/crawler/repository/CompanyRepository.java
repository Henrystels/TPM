package com.example.crawler.repository;

import com.example.crawler.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.phones WHERE c.website = :website")
    Optional<Company> findByWebsite(@Param("website") String website);
    
    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.phones WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Company> findByNameContainingIgnoreCase(@Param("name") String name);
    
    @Query("SELECT DISTINCT c FROM Company c WHERE SIZE(c.phones) > 0 OR SIZE(c.emails) > 0")
    List<Company> findCompaniesWithContacts();
    
    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.phones WHERE :phone MEMBER OF c.phones")
    List<Company> findByPhonesContaining(@Param("phone") String phone);
    
    @Query("SELECT DISTINCT c FROM Company c LEFT JOIN FETCH c.emails WHERE :email MEMBER OF c.emails")
    List<Company> findByEmailsContaining(@Param("email") String email);
}


