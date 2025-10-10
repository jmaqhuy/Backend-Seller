package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "image_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class ImageCustomization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "instructions", columnDefinition = "TEXT", length = 200)
    private String instructions;

    @Column(name = "x", nullable = false)
    private Integer x;

    @Column(name = "y", nullable = false)
    private Integer y;

    @Column(name = "width", nullable = false)
    private Integer width;

    @Column(name = "height", nullable = false)
    private Integer height;
}
