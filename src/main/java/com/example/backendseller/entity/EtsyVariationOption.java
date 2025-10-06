package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etsy_variation_option")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyVariationOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etsy_variation_id", insertable = false, updatable = false)
    private Long etsyVariationId;

    @Column(name = "label", length = 255)
    private String label;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etsy_variation_id", referencedColumnName = "id")
    private EtsyVariation etsyVariation;
}