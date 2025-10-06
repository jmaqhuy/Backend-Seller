package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyProductPersonalization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtsyProductPersonalizationRepository extends JpaRepository<EtsyProductPersonalization, Long> {
}
