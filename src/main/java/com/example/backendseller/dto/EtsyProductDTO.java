package com.example.backendseller.dto;

import com.example.backendseller.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EtsyProductDTO {
    private Long id;
    private String title;
    private Double price;
    private String material;
    private String generateStatus;
    private String tags;
    private Integer acc;
    private ProductType productType;
    private String etsyImage;

    public static EtsyProductDTO fromEntity(EtsyProduct product) {
        return EtsyProductDTO.builder()
                .id(product.getId())
                .build();
    }
}
