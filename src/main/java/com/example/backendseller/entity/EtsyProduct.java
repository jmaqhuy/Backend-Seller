package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etsy_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyProduct {

    @Id
    private Long id;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "price")
    private Double price;

    @Column(name = "material", length = 255)
    private String material;

    @Enumerated(EnumType.STRING)
    @Column(name = "generate_status")
    private GenerateStatus generateStatus;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "etsy_product_tag",
            joinColumns = @JoinColumn(name = "etsy_product_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags = new ArrayList<>();

    @Column(name = "acc")
    private Integer acc;

    @Column(name = "product_type_id", insertable = false, updatable = false)
    private Integer productTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyImage> etsyImages;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyProductPersonalization> personalizations;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyVariation> variations;

    public enum GenerateStatus {
        PENDING,
        PROCESSING,
        COMPLETED,
        FAILED
    }

    @Override
    public String toString() {
        return "EtsyProduct{id=" + id + ", title='" + title + "', price=" + price + ", material='" + material + "', generateStatus=" + generateStatus + ", acc=" + acc + "}";
    }
}