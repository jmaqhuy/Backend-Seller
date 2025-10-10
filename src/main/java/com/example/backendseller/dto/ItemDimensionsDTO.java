package com.example.backendseller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemDimensionsDTO {
    @JsonProperty("item_depth_front_to_back")
    private Double itemDepthFrontToBack;
    @JsonProperty("item_height_floor_to_top")
    private Double itemHeightFloorToTop;
    @JsonProperty("item_width_side_to_side")
    private Double itemWidthSideToSide;
    private String unit;
}