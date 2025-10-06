package com.example.backendseller.dto.openai.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetail {
    private String title;
    private String description;
    @JsonProperty("bullet_points")
    private List<String> bulletPoints;
    private String shape;
    private List<String> material;
    private String tags;
    private List<Customization> customizations;
}
