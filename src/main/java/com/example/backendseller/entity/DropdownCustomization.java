package com.example.backendseller.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "dropdown_customizations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DropdownCustomization {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "type", nullable = false, length = 50)
    private String type;

    @Column(name = "label", nullable = false, length = 100)
    private String label;

    @Column(name = "instructions", columnDefinition = "TEXT", length = 200)
    private String instructions;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "dropdown_customization_id")
    private List<Option> options;
}