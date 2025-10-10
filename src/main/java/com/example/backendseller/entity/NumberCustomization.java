package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "number_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class NumberCustomization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "instructions", columnDefinition = "TEXT", length = 200)
    private String instructions;

    @Column(name = "min_value", nullable = false)
    private Integer minValue;

    @Column(name = "max_value", nullable = false)
    private Integer maxValue;

    @Column(name = "placeholder")
    private Integer placeholder;
}