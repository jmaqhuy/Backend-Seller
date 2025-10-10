package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "amazon_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class AmazonProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "etsy_product_id", insertable = false, updatable = false)
    private Long etsyProductId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "etsy_product_id", referencedColumnName = "id")
    private EtsyProduct etsyProduct;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "shape", nullable = false)
    private String shape;

    @Column(name = "tags", columnDefinition = "TEXT", nullable = false)
    private String tags;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "amazon_product_bullet_points", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "bullet_point")
    @OrderColumn(name = "bullet_index")
    private List<String> bulletPoints;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "amazon_product_materials", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "material")
    private Set<String> material;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "amazon_product_target_audience", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "audience")
    private Set<String> targetAudience;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "amazon_product_occasion", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "occasion_name")
    private Set<String> occasion;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "itemDepthFrontToBack", column = @Column(name = "item_depth_front_to_back")),
            @AttributeOverride(name = "itemHeightFloorToTop", column = @Column(name = "item_height_floor_to_top")),
            @AttributeOverride(name = "itemWidthSideToSide", column = @Column(name = "item_width_side_to_side")),
            @AttributeOverride(name = "unit", column = @Column(name = "dimension_unit"))
    })
    private ItemDimensions itemDimensions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private List<DataCustomization> dataCustomizations;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private List<DropdownCustomization> dropdownCustomizations;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private List<NumberCustomization> numberCustomizations;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private List<ImageCustomization> imageCustomizations;



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
}
