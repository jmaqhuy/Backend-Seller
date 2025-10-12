package com.example.backendseller.controller;

import com.example.backendseller.dto.CustomResponse;
import com.example.backendseller.dto.ProductTypeDTO;
import com.example.backendseller.entity.AmazonAccount;
import com.example.backendseller.service.AmazonAccountService;
import com.example.backendseller.service.ProductTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/extension")
@RequiredArgsConstructor
public class ExtensionController {
    private final AmazonAccountService amazonAccountService;
    private final ProductTypeService productTypeService;

    @GetMapping("/data")
    public ResponseEntity<CustomResponse> getAccountsAndProductTypes() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        List<AmazonAccount> accounts = amazonAccountService.getAllAccounts(pageable);
        List<ProductTypeDTO> productTypes = productTypeService.getAllProductTypes(pageable);

        return ResponseEntity.ok(
                CustomResponse.builder()
                        .data(Map.of(
                                "account", accounts,
                                "productType", productTypes
                        ))
                        .message("Get data success.")
                        .build()
        );
    }
}
