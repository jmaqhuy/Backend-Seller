package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EtsyImageRepository extends JpaRepository<EtsyImage, Long> {
}