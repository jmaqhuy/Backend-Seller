package com.example.backendseller.repository;

import com.example.backendseller.entity.ProductType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductTypeRepository extends JpaRepository<ProductType, Integer> {
    Optional<ProductType> findByName(String name);
}