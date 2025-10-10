package com.example.backendseller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("Number")
public class NumberCustomizationDTO {
    private String type;
    private String label;
    private String instructions;
    @JsonProperty("min_value")
    private Integer minValue;
    @JsonProperty("max_value")
    private Integer maxValue;
    private Integer placeholder;
}