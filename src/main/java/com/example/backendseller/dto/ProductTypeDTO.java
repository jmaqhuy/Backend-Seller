package com.example.backendseller.dto;

import com.example.backendseller.entity.ProductType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTypeDTO {
    private Integer id;
    private String name;

    public static ProductTypeDTO fromEntity(ProductType productType) {
        return ProductTypeDTO.builder()
                .id(productType.getId())
                .name(productType.getName())
                .build();
    }
}
