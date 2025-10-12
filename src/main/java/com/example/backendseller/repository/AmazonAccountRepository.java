package com.example.backendseller.repository;

import com.example.backendseller.entity.AmazonAccount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmazonAccountRepository extends JpaRepository<AmazonAccount, Integer> {
    Page<AmazonAccount> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Page<AmazonAccount> findByAbbreviationContainingIgnoreCase(String abbreviation, Pageable pageable);
    Page<AmazonAccount> findByGoogleSheetIdContainingIgnoreCase(String googleSheetId, Pageable pageable);
}
