package com.example.backendseller.dto;

import com.example.backendseller.entity.EtsyProduct;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateEtsyProductRequest {
    private Long id;
    private String url;
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
    private Shop shop;

    public static CreateEtsyProductRequest fromEntity(EtsyProduct etsyProduct) {
        return CreateEtsyProductRequest.builder()
                .id(etsyProduct.getId())
                .title(etsyProduct.getTitle())
                .price(etsyProduct.getPrice())
                .material(etsyProduct.getMaterial())
                .productType(etsyProduct.getProductType().getName())
                .acc(etsyProduct.getAcc())
                .build();
    }
    @Data
    public static class Shop {
        private String name;
        private String url;
        private String image;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Chuyển object thành JSON string
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
            return "{}"; // fallback nếu có lỗi
        }
    }
}