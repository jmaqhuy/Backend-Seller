package com.example.backendseller.dto;

import com.example.backendseller.entity.*;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

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
    private List<String> tags = new ArrayList<>();
    private String amazonAccount;
    private String productType;
    private CreateEtsyProductRequest.Shop etsyShop;
    private List<EtsyImage> etsyImages;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyProductPersonalization> personalizations;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyVariation> variations;

    @OneToOne(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AmazonProduct amazonProduct;

    public static EtsyProductDTO fromEntity(EtsyProduct product) {
        return EtsyProductDTO.builder()
                .id(product.getId())
                .build();
    }
}
