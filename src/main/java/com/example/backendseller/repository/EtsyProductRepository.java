package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtsyProductRepository extends JpaRepository<EtsyProduct, Long> {
}
