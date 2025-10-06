package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Entity
@Table(name = "product_type")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name", length = 255)
    private String name;

    @OneToMany(mappedBy = "productType", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyProduct> etsyProducts;
}