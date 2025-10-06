package com.example.backendseller.service;

import com.example.backendseller.dto.openai.request.OpenAIRequest;
import com.example.backendseller.dto.openai.response.OpenAIResponse;
import com.example.backendseller.dto.openai.response.ProductDetail;
import com.example.backendseller.entity.EtsyProduct;
import com.example.backendseller.entity.EtsyProductPersonalization;
import com.example.backendseller.entity.EtsyVariation;
import com.example.backendseller.entity.Tag;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.Year;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OpenAiService {
    private static final int MAX_TAGS_BYTES = 500;
    private static final String TAG_SEPARATOR = ";";
    @Value("${openai.api.model}")
    private String model;

    @Value("${openai.api.token}")
    private String apiKey;

    @Value("${openai.api.url}")
    private String OPENAI_API_URL;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final EtsyProductService etsyProductService;

    @Async("taskExecutor")
    @Transactional
    public CompletableFuture<Void> generateProductContent(Long productId) {
        log.info("Starting async generation for product: {}", productId);

        try {
            // Cập nhật status sang PROCESSING
            etsyProductService.updateGenerateStatus(productId, EtsyProduct.GenerateStatus.PROCESSING);

            // Simulate generation process (thay bằng logic thực tế của bạn)
            // Ví dụ: gọi AI API, xử lý ảnh, generate description, etc.
            simulateGeneration(productId);

            // Cập nhật status sang COMPLETED
            etsyProductService.updateGenerateStatus(productId, EtsyProduct.GenerateStatus.COMPLETED);

            log.info("Completed generation for product: {}", productId);
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed to generate content for product: {}", productId, e);

            // Cập nhật status sang FAILED
            etsyProductService.updateGenerateStatus(productId, EtsyProduct.GenerateStatus.FAILED);

            return CompletableFuture.failedFuture(e);
        }
    }
    /**
     * Generate content cho nhiều sản phẩm (async batch)
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> generateMultipleProducts(List<Long> productIds) {
        log.info("Starting batch generation for {} products", productIds.size());

        try {
            for (Long productId : productIds) {
                try {
                    generateProductContent(productId).get(); // Wait for each to complete
                } catch (Exception e) {
                    log.error("Failed to generate product {}: {}", productId, e.getMessage());
                    // Continue with next product
                }
            }

            log.info("Completed batch generation for {} products", productIds.size());
            return CompletableFuture.completedFuture(null);

        } catch (Exception e) {
            log.error("Failed batch generation", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Generate tất cả sản phẩm PENDING (async)
     */
    @Async("taskExecutor")
    public CompletableFuture<Integer> generateAllPendingProducts() {
        log.info("Starting generation for all pending products");

        try {
            List<EtsyProduct> pendingProducts = etsyProductService.getPendingProducts();
            int totalProcessed = 0;

            for (EtsyProduct product : pendingProducts) {
                try {
                    generateProductContent(product.getId()).get();
                    totalProcessed++;
                } catch (Exception e) {
                    log.error("Failed to generate product {}: {}", product.getId(), e.getMessage());
                }
            }

            log.info("Completed generation for {} pending products", totalProcessed);
            return CompletableFuture.completedFuture(totalProcessed);

        } catch (Exception e) {
            log.error("Failed to generate pending products", e);
            return CompletableFuture.failedFuture(e);
        }
    }

    /**
     * Simulate generation process (thay bằng logic thực tế)
     */
    private void simulateGeneration(Long productId) {
        log.info("Generating content for product: {}", productId);
        log.info("Step 1: Start simulateGeneration() for product {}", productId);

        EtsyProduct product = null;
        try {
            product = etsyProductService.getProduct(productId);
            log.info("Step 2: Product found: {}", product);
        } catch (Exception e) {
            log.error("Error loading product {}: {}", productId, e.getMessage(), e);
            throw e;
        }

        String userPrompt = null;
        try {
            log.info("Step 2.5: About to call getUserPrompt()");
            userPrompt = getUserPrompt(product);
            log.info("Step 3: User prompt generated: {}", userPrompt);
        } catch (Exception e) {
            log.error("Error in getUserPrompt for product {}: {}", productId, e.getMessage(), e);
            e.printStackTrace(System.err);  // Force in ra console ngay lập tức
            System.err.println("=== FULL STACK TRACE ===");
            e.printStackTrace();
            System.err.println("=== END STACK TRACE ===");
            throw new RuntimeException("Failed in getUserPrompt", e);  // Wrap và re-throw để outer catch thấy
        }

        String systemPrompt = null;
        try {
            log.info("Step 3.5: About to call getSystemPrompt()");
            systemPrompt = getSystemPrompt();
            log.info("Step 4: System prompt: {}", systemPrompt);
        } catch (Exception e) {
            log.error("Error in getSystemPrompt: {}", e.getMessage(), e);
            throw e;
        }

        String imageUrl = null;
        try {
            log.info("Step 4.5: About to get imageUrl");
            imageUrl = product.getEtsyImages().getFirst().getUrl();  // Có thể throw ở đây nếu etsyImages lazy
            log.info("Step 4.8: Image URL: {}", imageUrl);
        } catch (Exception e) {
            log.error("Error getting imageUrl for product {}: {}", productId, e.getMessage(), e);
            throw e;
        }

        ProductDetail detail = null;
        try {
            log.info("Step 5: About to call callApi");
            detail = callApi(systemPrompt, userPrompt, imageUrl);
            log.info("Step 5: Got response: {}", detail);
        } catch (Exception e) {
            log.error("Error in callApi for product {}: {}", productId, e.getMessage(), e);
            throw e;
        }

        log.info("Generation completed for product: {}", productId);
    }

    private ProductDetail callApi(String systemPrompt, String userPrompt, String imageUrl) {
        try {
            OpenAIRequest request = OpenAIRequest.createProductRequest(
                    model, systemPrompt, userPrompt, imageUrl
            );
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");
            HttpEntity<OpenAIRequest> entity = new HttpEntity<>(request, headers);

            log.debug("Sending request to OpenAI API: {}", OPENAI_API_URL);

            ResponseEntity<OpenAIResponse> response = restTemplate.exchange(
                    OPENAI_API_URL,
                    HttpMethod.POST,
                    entity,
                    OpenAIResponse.class
            );

            String jsonContent = getString(response);
            log.debug("Received JSON content from OpenAI");

            return objectMapper.readValue(jsonContent, ProductDetail.class);
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }

    private static String getString(ResponseEntity<OpenAIResponse> response) {
        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("OpenAI API call failed with status: " + response.getStatusCode());
        }

        OpenAIResponse openAIResponse = response.getBody();
        if (openAIResponse == null || openAIResponse.getOutput() == null || openAIResponse.getOutput().isEmpty()) {
            throw new RuntimeException("No output received from OpenAI API");
        }

        return openAIResponse.getOutput().getFirst().getContent().getFirst().getText();
    }

    /**
     * Kiểm tra status generation của một sản phẩm
     */
    public EtsyProduct.GenerateStatus checkGenerationStatus(Long productId) {
        EtsyProduct product = etsyProductService.getProduct(productId);
        return product.getGenerateStatus();
    }

    /**
     * Retry generation cho sản phẩm FAILED
     */
    @Async("taskExecutor")
    public CompletableFuture<Void> retryFailedProduct(Long productId) {
        log.info("Retrying generation for failed product: {}", productId);

        EtsyProduct product = etsyProductService.getProduct(productId);

        if (product.getGenerateStatus() != EtsyProduct.GenerateStatus.FAILED) {
            log.warn("Product {} is not in FAILED status, skipping retry", productId);
            return CompletableFuture.completedFuture(null);
        }

        return generateProductContent(productId);
    }

    private String getUserPrompt(EtsyProduct product) {
        log.info("Inside getUserPrompt: product title = {}", product.getTitle());  // Safe, không lazy

        // Test từng phần một
        String tagsStr = "";
        try {
            log.info("About to access product.getTags()");
            if (product.getTags() != null) {
                tagsStr = product.getTags().stream().map(Tag::getName).collect(Collectors.joining(","));  // Safe hơn toString()
            } else {
                tagsStr = "No tags";
            }
            log.info("Tags accessed: {}", tagsStr);
        } catch (Exception e) {
            log.error("Error accessing tags: {}", e.getMessage(), e);
            e.printStackTrace(System.err);
            tagsStr = "Error in tags";  // Fallback
        }

        String materialStr = product.getMaterial() != null ? product.getMaterial() : "No material";
        log.info("Material accessed: {}", materialStr);

        String personalizeStr = "No";
        try {
            log.info("About to access product.getPersonalizations()");
            if (product.getPersonalizations() != null) {
                personalizeStr = product.getPersonalizations().stream().map(EtsyProductPersonalization::getContent).collect(Collectors.joining(","));
            }
            log.info("Personalizations accessed: {}", personalizeStr);
        } catch (Exception e) {
            log.error("Error accessing personalizations: {}", e.getMessage(), e);
            personalizeStr = "Error in personalizations";
        }

        String variationsStr = "No";
        try {
            log.info("About to access product.getVariations()");
            if (product.getVariations() != null) {
                variationsStr = product.getVariations().stream().map(EtsyVariation::getLabel).collect(Collectors.joining(","));
            }
            log.info("Variations accessed: {}", variationsStr);
        } catch (Exception e) {
            log.error("Error accessing variations: {}", e.getMessage(), e);
            variationsStr = "Error in variations";
        }

        // Tiếp tục build prompt với fallback values
        String prompt = String.format("""
            Help me to write optimized a product listing for my Amazon store based on the following details and customization (if need) based on personalize (if exist) and variations(if exist) :
            - Product name: "%s"
            - Tags: %s
            - Material: %s
            - Personalize: %s
            - Variations: %s
            Follow these requirements:
            - ensure the total length of tags does not exceed %d bytes.
            - If the draft title contains a year, replace it with the current year: %d. If not, do not add the current year.
            - Always treat as physical product, even if draft indicates digital. Do not mention digital in any part of the listing.
            - If the Generic keyword contains digital-product-related terms (e.g., svg, file, laser file), generate a new list of relevant physical product keywords based on the current tags; otherwise, change the order of each. Each generic keyword separate by ';'. Generic keywords should be less than 500 bytes long, if current is shorter, please add more.
            """,
                escapeJson(product.getTitle()),
                escapeJson(tagsStr),
                escapeJson(materialStr),
                escapeJson(personalizeStr),
                escapeJson(variationsStr),
                MAX_TAGS_BYTES,
                Year.now().getValue());

        log.info("User prompt built successfully: length = {}", prompt.length());
        return prompt;
    }

    private String escapeJson(String value) {
        if (value == null) return "";
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String getSystemPrompt() {
        return """
                {
                  "role": "You are an expert e-commerce content creator specializing in physical products for Amazon listings.",
                  "language": "English",
                  "amazon_policies": {
                    "general_requirements": {
                      "product_type": "physical products only",
                      "content_must_be": [
                        "clear and persuasive",
                        "keyword-rich",
                        "tailored to target audience",
                        "focused on discoverability and conversion",
                        "100%% Amazon compliant"
                      ],
                      "prohibited_elements": [
                        "digital files",
                        "prohibited terms",
                        "brand names",
                        "ordering instructions",
                        "external links",
                        "coupon codes"
                      ]
                    },
                    "tags": {
                      "another_term": [
                        "generic_keyword",
                        "search terms",
                        "keywords"
                      ],
                      "character_limit": {
                        "minimum": 300,
                        "maximum": 500,
                        "reason_for_limit": "Amazon allows up to 500 bytes, but more can improve discoverability"
                      },
                      "formatting_rules": {
                        "separator": ";"
                      }
                    },
                    "product_title": {
                      "character_limit": {
                        "maximum": 200,
                        "recommended": 80,
                        "reason_for_recommendation": "mobile screens truncate long titles"
                      },
                      "prohibited_content": {
                        "special_word": [
                          "gift"
                        ],
                        "promotional_phrases": [
                          "free shipping",
                          "100%% quality guaranteed"
                        ],
                        "special_characters": [
                          "!", "$", "?", "_", "{", "}", "^", "¬", "¦"
                        ],
                        "subjective_commentary": [
                          "Hot Item",
                          "Best seller"
                        ],
                        "formatting": [
                          "ALL CAPS",
                          "redundant information",
                          "unnecessary synonyms",
                          "excessive keywords"
                        ]
                      },
                      "word_repetition_rule": {
                        "maximum_same_word": 2,
                        "case_sensitivity": "case-insensitive",
                        "duplicated_words": "forbidden"
                      },
                      "formatting_rules": {
                        "capitalization": {
                          "rule": "capitalize first letter of each word",
                          "exceptions": {
                            "prepositions": [
                              "in", "on", "over", "with"
                            ],
                            "conjunctions": [
                              "and", "or", "for"
                            ],
                            "articles": [
                              "the", "a", "an"
                            ]
                          }
                        },
                        "numbers": "use numerals (2 instead of two)",
                        "character_set": "standard letters and numbers only",
                        "allowed_punctuation": [
                          "-", "/", ",", "&", "."
                        ]
                      }
                    },
                    "bullet_points": {
                      "character_limits": {
                        "minimum": 10,
                        "maximum": 255
                      },
                      "prohibited_content": {
                        "special_characters": [
                          "™", "®", "€", "…", "†", "‡", "¢", "£", "¥", "©", "±", "~"
                        ],
                        "emojis": [
                          "☺", "☹", "✅", "❌"
                        ],
                        "identifiers": [
                          "ASIN number",
                          "not applicable",
                          "NA",
                          "n/a",
                          "N/A",
                          "TBD",
                          "COPY PENDING"
                        ],
                        "prohibited_phrases": [
                          "eco-friendly",
                          "environmentally friendly",
                          "anti-microbial",
                          "anti-bacterial",
                          "Made from Bamboo",
                          "Made from Soy"
                        ],
                        "guarantee_information": [
                          "Full refund",
                          "If not satisfied, send it back",
                          "Unconditional guarantee"
                        ],
                        "contact_information": [
                          "company information",
                          "website links",
                          "external hyperlinks"
                        ]
                      },
                      "formatting_requirements": {
                        "structure": "add header to bullet point and use ':' as separator",
                        "capitalization": "begin with capital letter",
                        "format": "sentence fragment (no end punctuation)",
                        "phrase_separation": "use semicolons to separate phrases within single bullet point",
                        "number_format": "write numbers one to nine in full, excluding names, model numbers, and measurements",
                        "uniqueness": "each bullet point must mention unique product information"
                      }
                    }
                  },
                  "customization": {
                    "accept_type": [
                      "Option Dropdown",
                      "Data",
                      "Number",
                      "Image"
                    ],
                    "label": "Simple word like name, shape, color, material, number, year, length, weight, date...",
                    "options": "provide when accept_type is Option Dropdown",
                    "example": [
                      {
                        "type": "Option Dropdown",
                        "label": "Shape",
                        "options": [
                          "Circle",
                          "Heart"
                        ]
                      },
                      {
                        "type": "Data",
                        "label": "Name"
                      }
                    ]
                  }
                }
                """;
    }


}
