package com.example.backendseller.dto;

import com.example.backendseller.entity.EtsyProduct;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtsyProductDTO {
    private Long id;
    private String title;
    private String description;
    private Double price;
    private List<String> images;
    private String material;
    private List<EtsyVariationDTO> variation;
    private List<String> personalization;
    private List<String> tags;
    private String productType;
    private Integer acc;

    public static EtsyProductDTO fromEntity(EtsyProduct etsyProduct) {
        return EtsyProductDTO.builder()
                .id(etsyProduct.getId())
                .title(etsyProduct.getTitle())
                .price(etsyProduct.getPrice())
                .material(etsyProduct.getMaterial())
                .productType(etsyProduct.getProductType().getName())
                .acc(etsyProduct.getAcc())
                .build();
    }
}