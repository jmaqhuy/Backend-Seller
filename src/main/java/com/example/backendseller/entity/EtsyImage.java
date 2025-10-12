package com.example.backendseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etsy_image")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @Column(name = "etsy_product_id", insertable = false, updatable = false)
    private Long etsyProductId;

    @Column(name = "url", length = 255)
    private String url;

    @Column(name = "display_order")
    private Integer displayOrder;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etsy_product_id", referencedColumnName = "id")
    private EtsyProduct etsyProduct;
}