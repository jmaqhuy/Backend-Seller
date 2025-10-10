package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "data_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DataCustomization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "instructions", columnDefinition = "TEXT", length = 200)
    private String instructions;

    @Column(name = "sample_text", length = 30)
    private String sampleText;

    @Column(name = "min_characters", nullable = false)
    private Integer minCharacters;

    @Column(name = "max_characters", nullable = false)
    private Integer maxCharacters;

    @Column(name = "lines_allowed", nullable = false)
    private Integer linesAllowed;
}