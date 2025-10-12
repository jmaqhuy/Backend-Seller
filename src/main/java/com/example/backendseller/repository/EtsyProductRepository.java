package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtsyProductRepository extends JpaRepository<EtsyProduct, Long> {

    @Query("SELECT ep FROM EtsyProduct ep LEFT JOIN FETCH ep.tags " +
            "LEFT JOIN FETCH ep.etsyImages LEFT JOIN FETCH ep.personalizations " +
            "LEFT JOIN FETCH ep.variations LEFT JOIN FETCH ep.productType " +
            "LEFT JOIN FETCH ep.etsyShop " +
            "LEFT JOIN FETCH ep.amazonAccount")
    Page<EtsyProduct> findAllWithDetails(Pageable pageable);

    @Query("SELECT ep FROM EtsyProduct ep LEFT JOIN FETCH ep.tags " +
            "LEFT JOIN FETCH ep.etsyImages LEFT JOIN FETCH ep.personalizations " +
            "LEFT JOIN FETCH ep.variations LEFT JOIN FETCH ep.productType " +
            "LEFT JOIN FETCH ep.etsyShop " +
            "LEFT JOIN FETCH ep.amazonAccount " +
            "WHERE ep.amazonAccount.id = :accountId")
    Page<EtsyProduct> findByAmazonAccountWithDetails(
            @Param("accountId") Integer accountId,
            Pageable pageable);

    @Query("SELECT ep FROM EtsyProduct ep LEFT JOIN FETCH ep.tags " +
            "LEFT JOIN FETCH ep.etsyImages LEFT JOIN FETCH ep.personalizations " +
            "LEFT JOIN FETCH ep.variations LEFT JOIN FETCH ep.productType " +
            "LEFT JOIN FETCH ep.etsyShop " +
            "LEFT JOIN FETCH ep.amazonAccount " +
            "WHERE ep.generateStatus = :status")
    Page<EtsyProduct> findByGenerateStatusWithDetails(
            @Param("status") EtsyProduct.GenerateStatus status,
            Pageable pageable);

    List<EtsyProduct> findByGenerateStatus(EtsyProduct.GenerateStatus status);

    long countByGenerateStatus(EtsyProduct.GenerateStatus status);
}
