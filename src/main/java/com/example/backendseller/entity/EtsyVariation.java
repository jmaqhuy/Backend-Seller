package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "etsy_variation")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyVariation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etsy_product_id", insertable = false, updatable = false)
    private Long etsyProductId;

    @Column(name = "label", length = 255)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etsy_product_id", referencedColumnName = "id")
    private EtsyProduct etsyProduct;

    @OneToMany(mappedBy = "etsyVariation", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyVariationOption> variationOptions;
}