package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyVariationOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtsyVariationOptionRepository extends JpaRepository<EtsyVariationOption, Long> {
}