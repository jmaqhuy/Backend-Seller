package com.example.backendseller.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDimensions {
    @Column(name = "item_depth_front_to_back")
    private Double itemDepthFrontToBack;

    @Column(name = "item_height_floor_to_top")
    private Double itemHeightFloorToTop;

    @Column(name = "item_width_side_to_side")
    private Double itemWidthSideToSide;

    @Column(name = "dimension_unit")
    private String unit;
}