package com.example.backendseller.service;

import com.example.backendseller.dto.ProductTypeDTO;
import com.example.backendseller.entity.ProductType;
import com.example.backendseller.repository.ProductTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductTypeService {
    private final ProductTypeRepository productTypeRepository;

    @Cacheable(
            value = "productTypes",
            key = "'all_' + #pageable.pageNumber + '_' + #pageable.pageSize",
            unless = "#result == null || #result.isEmpty()"
    )
    public List<ProductTypeDTO> getAllProductTypes(Pageable pageable) {
        return productTypeRepository.findAll(pageable)
                .getContent()
                .stream()
                .map(ProductTypeDTO::fromEntity)
                .toList();
    }

    public ProductType getProductTypeByName(String name){
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Product type name cannot be empty");
        }
        return productTypeRepository.findByName(name).orElseThrow(
                () -> new IllegalArgumentException("Product type name " + name + " not found")
        );
    }

    public ProductType getProductTypeById(Integer id) {
        return productTypeRepository.findById(id).orElseThrow(
                () -> new IllegalArgumentException("Product type id " + id + " not found")
        );
    }

    @CacheEvict(value = {"productTypes"}, allEntries = true)
    public ProductType createProductType(String name){
        if (!StringUtils.hasText(name)) {
            throw new IllegalArgumentException("Product type name cannot be empty");
        }
        if (productTypeRepository.findByName(name).isPresent()) {
            throw new IllegalArgumentException("Product type already exists");
        }
        return productTypeRepository.save(ProductType.builder().name(name).build());
    }
}
