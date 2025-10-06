package com.example.backendseller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyVariationDTO {
    private String label;
    private List<String> options;
}
