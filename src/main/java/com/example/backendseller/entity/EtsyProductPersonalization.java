package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "etsy_product_personalization")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyProductPersonalization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "etsy_product_id", insertable = false, updatable = false)
    private Long etsyProductId;

    @Column(name = "line_order")
    private Integer lineOrder;

    @Column(name = "content", length = 255)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etsy_product_id", referencedColumnName = "id")
    private EtsyProduct etsyProduct;
}