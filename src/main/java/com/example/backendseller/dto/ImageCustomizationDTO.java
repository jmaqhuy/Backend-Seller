package com.example.backendseller.dto;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

@Data
@JsonTypeName("Image")
public class ImageCustomizationDTO {
    private String type;
    private String label;
    private String instructions;
    private Integer x;
    private Integer y;
    private Integer width;
    private Integer height;
}