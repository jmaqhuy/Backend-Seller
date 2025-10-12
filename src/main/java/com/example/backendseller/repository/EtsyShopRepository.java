package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyShop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface EtsyShopRepository extends JpaRepository<EtsyShop, Long> {
    Optional<EtsyShop> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
}
