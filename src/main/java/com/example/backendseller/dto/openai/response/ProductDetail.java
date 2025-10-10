package com.example.backendseller.dto.openai.response;

import com.example.backendseller.dto.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ProductDetail {
    private String title;
    private String description;
    @JsonProperty("bullet_points")
    private List<String> bulletPoints;
    private String shape;
    private List<String> material;
    private String tags;
    private List<String> target_audience;
    private List<String> occasion;
    private ItemDimensionsDTO item_dimensions;
    @JsonProperty("data_customizations")
    private List<DataCustomizationDTO> dataCustomizations;
    @JsonProperty("dropdown_customizations")
    private List<DropdownCustomizationDTO> dropdownCustomizations;
    @JsonProperty("number_customizations")
    private List<NumberCustomizationDTO> numberCustomizations;
    @JsonProperty("image_customizations")
    private List<ImageCustomizationDTO> imageCustomizations;
}
