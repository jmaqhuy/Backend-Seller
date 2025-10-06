package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtsyVariationRepository extends JpaRepository<EtsyVariation, Long> {
}
