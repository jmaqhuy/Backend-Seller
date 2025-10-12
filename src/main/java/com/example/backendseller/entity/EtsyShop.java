package com.example.backendseller.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "etsy_shop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EtsyShop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 34, nullable = false)
    private String name;

    @Column(name = "url", length = 60, nullable = false)
    private String url;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "accept")
    private Boolean accept = Boolean.TRUE;

    @JsonIgnore
    @OneToMany(mappedBy = "etsyShop", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<EtsyProduct> products = new ArrayList<>();
}
