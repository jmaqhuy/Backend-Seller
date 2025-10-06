package com.example.backendseller.repository;

import com.example.backendseller.entity.EtsyProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtsyProductRepository extends JpaRepository<EtsyProduct, Long> {
    List<EtsyProduct> findByGenerateStatus(EtsyProduct.GenerateStatus status);

    // Tìm sản phẩm pending order by created date (nếu có field createdDate)
    // List<EtsyProduct> findByGenerateStatusOrderByCreatedDateAsc(EtsyProduct.GenerateStatus status);

    // Đếm số lượng sản phẩm theo status
    long countByGenerateStatus(EtsyProduct.GenerateStatus status);
}
