package com.example.backendseller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonTypeName("Data")
public class DataCustomizationDTO {
    private String type;
    private String label;
    private String instructions;
    @JsonProperty("sample_text")
    private String sampleText;
    @JsonProperty("min_characters")
    private Integer minCharacters;
    @JsonProperty("max_characters")
    private Integer maxCharacters;
    @JsonProperty("lines_allowed")
    private Integer linesAllowed;
}
