package com.example.backendseller.controller;

import com.example.backendseller.dto.CustomResponse;
import com.example.backendseller.entity.ProductType;
import com.example.backendseller.service.ProductTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ProductTypeController {
    private final ProductTypeService productTypeService;

    @PostMapping("/productTypes")
    public ResponseEntity<CustomResponse> createProductType(@RequestBody Map<String, String> productType) {
        try {
            ProductType newProductType = productTypeService.createProductType(productType.get("name"));
            return ResponseEntity.ok(
                    CustomResponse.builder()
                            .data(newProductType)
                            .message("Create Product Type Success")
                            .build()
            );
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    CustomResponse.builder()
                            .data(null)
                            .message(e.getMessage())
                            .build()
            );
        }
    }
}
