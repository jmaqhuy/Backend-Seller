package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
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

    @Column(name = "description", length = 2000)
    private String description;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyImage> etsyImages;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyProductPersonalization> personalizations;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EtsyVariation> variations;

    @OneToOne(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AmazonProduct amazonProduct;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

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