package com.example.backendseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "amazon_account_id", referencedColumnName = "id")
    private AmazonAccount amazonAccount;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_type_id", referencedColumnName = "id")
    private ProductType productType;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "etsy_shop_id", referencedColumnName = "id")
    private EtsyShop etsyShop;

    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyImage> etsyImages;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyProductPersonalization> personalizations;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @OneToMany(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyVariation> variations;

    @JsonIgnore
    @OneToOne(mappedBy = "etsyProduct", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private AmazonProduct amazonProduct;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
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
}