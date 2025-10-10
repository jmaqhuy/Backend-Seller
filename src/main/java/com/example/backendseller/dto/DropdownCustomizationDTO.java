package com.example.backendseller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Data;

import java.util.List;

@Data
@JsonTypeName("Option Dropdown")
public class DropdownCustomizationDTO {
    private String type;
    private String label;
    private String instructions;
    private List<DropdownOption> options;

    @Data
    public static class DropdownOption {
        private String name;
        @JsonProperty("price_difference")
        private Double priceDifference;
    }
}