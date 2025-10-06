package com.example.backendseller.controller;

import com.example.backendseller.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/etsy/products/generate")
@RequiredArgsConstructor
@Slf4j
public class ProductGenerateController {
    private final OpenAiService openAiService;

    @PostMapping("/{id}")
    public ResponseEntity<Map<String, Object>> generateProduct(@PathVariable Long id) {
        log.info("Received request to generate product: {}", id);

        try {
            // Start async generation
            openAiService.generateProductContent(id);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Generation started");
            response.put("productId", id);
            response.put("status", "PROCESSING");

            return ResponseEntity.accepted().body(response);

        } catch (Exception e) {
            log.error("Failed to start generation for product: {}", id, e);

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to start generation");
            errorResponse.put("message", e.getMessage());

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
