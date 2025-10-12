package com.example.backendseller.controller;

import com.example.backendseller.dto.CustomResponse;
import com.example.backendseller.dto.CreateEtsyProductRequest;
import com.example.backendseller.dto.EtsyProductDTO;
import com.example.backendseller.entity.EtsyProduct;
import com.example.backendseller.service.EtsyProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/etsy/products")
@RequiredArgsConstructor
public class EtsyProductController {

    private final EtsyProductService productService;

    /**
     * Tạo sản phẩm mới
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<CustomResponse> createProduct(@RequestBody CreateEtsyProductRequest dto) {
        try {
            log.info("Creating product {}", dto);
            EtsyProduct savedProduct = productService.saveProduct(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(CustomResponse.<CreateEtsyProductRequest>builder()
//                            .data(CreateEtsyProductRequest.fromEntity(savedProduct))
                            .message("Saved product successfully with id " + savedProduct.getId())
                    .build());
        } catch (Exception e) {
            log.error("Error while saving product: {} with ID: {}", e.getMessage(), dto.getId());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    CustomResponse.builder()
                            .data(null)
                            .message(e.getMessage())
                            .build()
            );
        }
    }

    /**
     * Cập nhật sản phẩm
     * PUT /api/products/{id}
     */
//    @PutMapping("/{id}")
//    public ResponseEntity<EtsyProduct> updateProduct(
//            @PathVariable Long id,
//            @RequestBody EtsyProductDTO dto) {
//        try {
//            dto.setId(id);
//            EtsyProduct updatedProduct = productService.saveProduct(dto);
//            return ResponseEntity.ok(updatedProduct);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//    }

    /**
     * Lấy thông tin sản phẩm
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EtsyProduct> getProduct(@PathVariable Long id) {
        try {
            EtsyProduct product = productService.getProduct(id);
            return ResponseEntity.ok(product);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Lấy tất cả sản phẩm
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<CustomResponse> getAllProducts(
            @PageableDefault(page = 0, size = 10, sort = {"updatedAt"}, direction = Sort.Direction.DESC) Pageable pageable)
    {
        List<EtsyProduct> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(
                CustomResponse.builder()
                        .data(new PageImpl<>(products, pageable, products.size()))
                        .message("Get Etsy Product List Success")
                        .build()
        );
    }

    /**
     * Xóa sản phẩm
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        try {
            productService.deleteProduct(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    /**
     * Cập nhật trạng thái generate
     * PATCH /api/products/{id}/status
     */
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam EtsyProduct.GenerateStatus status) {
        try {
            productService.updateGenerateStatus(id, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
