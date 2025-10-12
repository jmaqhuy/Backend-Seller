package com.example.backendseller.service;

import com.example.backendseller.dto.openai.request.OpenAIRequest;
import com.example.backendseller.dto.openai.response.OpenAIResponse;
import com.example.backendseller.dto.openai.response.ProductDetail;
import com.example.backendseller.entity.*;
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
            assert response.getBody() != null;
            log.info("Token usage: {}", response.getBody().getUsage().getTotalTokens());
            ProductDetail productDetail = objectMapper.readValue(jsonContent, ProductDetail.class);

            // Validate customizations
//            validateCustomizations(productDetail.getCustomizations());

            return productDetail;
        } catch (Exception e) {
            log.error("Error calling OpenAI API", e);
            throw new RuntimeException("Failed to call OpenAI API", e);
        }
    }

//    private void validateCustomizations(List<BaseCustomization> customizations) {
//        if (customizations == null) return;
//
//        for (BaseCustomization custom : customizations) {
//            if (custom instanceof DataCustomization) {
//                DataCustomization data = (DataCustomization) custom;
//                if (data.getMinCharacters() == null || data.getMaxCharacters() == null || data.getLinesAllowed() == null) {
//                    throw new ValidationException("Data customization missing required fields");
//                }
//                if (data.getMinCharacters() < 1 || data.getMaxCharacters() > 500) {
//                    throw new ValidationException("Invalid character limits: " + data.getLabel());
//                }
//                if (data.getLinesAllowed() < 1 || data.getLinesAllowed() > 10) {
//                    throw new ValidationException("Lines allowed must be 1-10: " + data.getLabel());
//                }
//
//            } else if (custom instanceof DropdownCustomization) {
//                DropdownCustomization dropdown = (DropdownCustomization) custom;
//                if (dropdown.getOptions() == null || dropdown.getOptions().isEmpty()) {
//                    throw new ValidationException("Dropdown must have options: " + dropdown.getLabel());
//                }
//                for (DropdownCustomization.DropdownOption option : dropdown.getOptions()) {
//                    if (option.getName() == null || option.getName().length() > 40) {
//                        throw new ValidationException("Invalid option name: " + option.getName());
//                    }
//                    if (option.getPriceDifference() == null) {
//                        option.setPriceDifference(0.0); // Set default
//                    }
//                }
//
//            } else if (custom instanceof NumberCustomization) {
//                NumberCustomization number = (NumberCustomization) custom;
//                if (number.getMinValue() == null || number.getMaxValue() == null) {
//                    throw new ValidationException("Number customization missing min/max: " + number.getLabel());
//                }
//                if (number.getMinValue() >= number.getMaxValue()) {
//                    throw new ValidationException("minValue must be less than maxValue: " + number.getLabel());
//                }
//
//            } else if (custom instanceof ImageCustomization) {
//                ImageCustomization image = (ImageCustomization) custom;
//                if (image.getMaxFileSize() == null || image.getAllowedFormats() == null) {
//                    throw new ValidationException("Image customization missing required fields: " + image.getLabel());
//                }
//                if (image.getMaxFileSize() < 1 || image.getMaxFileSize() > 10) {
//                    throw new ValidationException("Max file size must be 1-10 MB: " + image.getLabel());
//                }
//            }
//        }
//    }

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

        // Tạo cấu trúc dữ liệu variations
        String variationsStr = "No";
        try {
            log.info("About to access product.getVariations()");
            if (product.getVariations() != null && !product.getVariations().isEmpty()) {
                // Format: Label1: option1, option2, option3 | Label2: optionA, optionB
                variationsStr = product.getVariations().stream()
                        .map(variation -> {
                            String label = variation.getLabel();
                            String options = "";

                            if (variation.getVariationOptions() != null && !variation.getVariationOptions().isEmpty()) {
                                options = variation.getVariationOptions().stream()
                                        .map(EtsyVariationOption::getLabel)
                                        .collect(Collectors.joining(", "));
                            }

                            return options.isEmpty() ? label : (label + ": " + options);
                        })
                        .collect(Collectors.joining(" | "));
            }
            log.info("Variations accessed: {}", variationsStr);
        } catch (Exception e) {
            log.error("Error accessing variations: {}", e.getMessage(), e);
            variationsStr = "Error in variations";
        }

        // Tiếp tục build prompt với fallback values
        String prompt = String.format("""
                Create an optimized Amazon product listing for a PHYSICAL PRODUCT with these details:
                
                Product Name: "%s"
                Product Description: "%s"
                Current Tags: %s
                Material Options: %s
                Personalization Available: %s
                Product Variations: %s
                
                CRITICAL REQUIREMENTS:
                1. Replace any year in title with %d (current year). Don't add year if not present.
                2. ALWAYS treat as physical product - remove ALL digital/downloadable references
                3. Tags must be %d-%d bytes, semicolon-separated
                4. If tags contain digital terms (svg, file, pdf, download, digital, printable, instant), REPLACE with physical product keywords based on the product category
                5. Create customization options from personalization/variations data - use appropriate type (Option Dropdown, Data, Number, Image)
                
                Generate professional, conversion-optimized content following Amazon's policies.
                """,
                escapeJson(product.getTitle()),
                escapeJson(product.getDescription()),
                escapeJson(tagsStr),
                escapeJson(materialStr),
                escapeJson(personalizeStr),
                escapeJson(variationsStr),
                Year.now().getValue(),
                400, // min tags bytes
                500 // max tags bytes
        );

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
                  "role": "Expert Amazon listing optimizer for physical products",
                  "expertise": [
                    "SEO-optimized content creation",
                    "Amazon policy compliance",
                    "Conversion-focused copywriting",
                    "Product customization setup"
                  ],
                  "output_requirements": {
                    "title": {
                      "max_length": 200,
                      "optimal_length": 80,
                      "rules": [
                        "Capitalize first letter of each word (except: in, on, at, the, a, an, and, or, for, with)",
                        "Use numerals (2 not two)",
                        "Strict: No repetition of any word allowed (case-insensitive)",
                        "No special chars except: - / , & .",
                        "No promotional terms: free shipping, best seller, hot item",
                        "No ALL CAPS or excessive keywords"
                      ]
                    },
                    "description": {
                      "style": "persuasive, benefit-focused",
                      "length": "comprehensive but concise",
                      "focus": "highlight physical product features, quality, uses"
                    },
                    "bullet_points": {
                      "count": 5,
                      "length": "200-255 characters each",
                      "format": "Header: Description with key details",
                      "rules": [
                        "Start with capital letter",
                        "Each point must be unique",
                        "Use semicolons to separate phrases within bullet",
                        "No end punctuation",
                        "No emojis or special symbols",
                        "Spell out numbers 1-9 (except measurements)"
                      ]
                    },
                    "tags": {
                      "length": "400-500 bytes",
                      "format": "keyword1;keyword2;keyword3",
                      "strategy": "If input contains digital terms, generate NEW physical product keywords based on category. Otherwise reorder existing keywords for freshness.",
                      "focus": "high-volume search terms, product category, use cases, occasions"
                    },
                    "material": {
                      "select": "1-2 most relevant from provided options",
                      "valid_options": ["wood", "acrylic", "ceramic", "glass"]
                    },
                    "shape": {
                      "derive": "from product name/variations if mentioned, otherwise leave as general descriptor",
                      "example_options": [
                        "Ball", "Bell", "Butterfly", "Diamond", "Flower", "Heart", "Hexagonal",
                        "Leaf", "Moon", "Mushroom", "Oblong", "Octagonal", "Oval",
                        "Rectangular", "Round", "Semicircular", "Skull",
                        "Snowflake", "Square", "Star", "Tree", "Triangular"
                      ]
                    },
                    "target_audience": {
                      "count": "from 1 to 5",
                      "definition": "Specify the target audience that the product is intended for",
                      "example": ["Adult", "Aunt", "Baby", "Boss", "Boy", "Brother", "Children", "Co Worker", "Coach", "Daughter", "Expecting", "Father", "Friend", "Girl", "Godparent", "Granddaughter", "Grandfather", "Grandmother", "Grandparents", "Grandson", "Group", "Husband", "Men", "Mother", "Nephew", "Niece", "Parents", "Sister", "Son", "Student", "Teacher", "Teens", "Toddler", "Uncle", "Wife", "Women"]
                    },
                    "occasion": {
                      "count": "from 1 to 5",
                      "definition": "Provide the holiday or major life event that the product is designed to commemorate or celebrate.",
                      "example": ["Anniversary", "Baby Shower", "Babys First Christmas", "Bachelor Party", "Bachelorette Party", "Baptism", "Birthday", "Bridal Shower", "Christmas", "Cinco de Mayo", "Diwali", "Earth Day", "Easter", "Eid al-Fitr", "Engagement", "Farewell", "Father's Day", "First Married Christmas", "Friendship Day", "Graduation", "Grandparents Day", "Halloween", "Hanukkah", "Homecoming", "Housewarming", "Independence Day", "Kwanzaa", "Labor Day", "Memorial Day", "Mother's Day", "New Year", "Prom", "Retirement", "Saint Nicholas Day", "St. Patrick's Day", "Thanksgiving", "Valentine's Day", "Wedding"]
                    },
                    "item_dimensions": {
                      "item_depth_front_to_back": "Provide the measurement of the item from front to back in an assembled state.",
                      "item_height_floor_to_top": "Provide the measurement of the item from the floor to the top in an assembled state.",
                      "item_width_side_to_side": "Provide the measurement from side to side of the front of the item in an assembled state.",
                      "unit": "Always use: Inches"
                    },
                    "data_customizations": {
                      "type": "array of Data customization objects",
                      "minItems": 0,
                      "maxItems": 15,
                      "description": "Text input fields for customer personalization",
                      "field_structure": {
                        "type": {"value": "Data", "required": true},
                        "label": {"description": "Clear, simple label (Name, Message, Text, etc.)", "required": true, "max_length": 100},
                        "instructions": {"description": "Helper text for customer (e.g., 'Enter your name as you want it engraved')", "required": false, "max_length": 200},
                        "sample_text": {"description": "Example text to guide customer (e.g., 'John Smith')", "required": false, "max_length": 30},
                        "min_characters": {"description": "Minimum characters allowed", "required": true, "default": 1, "minimum": 1},
                        "max_characters": {"description": "Maximum characters allowed", "required": true, "default": 100, "maximum": 1000},
                        "lines_allowed": {"description": "Number of text lines allowed", "required": true, "default": 1, "minimum": 1, "maximum": 10}
                      },
                      "examples": [
                        {
                          "type": "Data",
                          "label": "Name",
                          "instructions": "Enter name for personalization",
                          "sample_text": "John Smith",
                          "min_characters": 1,
                          "max_characters": 50,
                          "lines_allowed": 1
                        },
                        {
                          "type": "Data",
                          "label": "Message",
                          "instructions": "Your custom message",
                          "sample_text": "Happy Birthday!",
                          "min_characters": 5,
                          "max_characters": 200,
                          "lines_allowed": 3
                        }
                      ]
                    },
                    "dropdown_customizations": {
                      "type": "array of Option Dropdown customization objects",
                      "minItems": 0,
                      "maxItems": 15,
                      "description": "Dropdown menus with predefined choices",
                      "field_structure": {
                        "type": {"value": "Option Dropdown", "required": true},
                        "label": {"description": "Category name (Color, Size, Material, etc.)", "required": true, "max_length": 100},
                        "instructions": {"description": "Helper text for selection", "required": false, "max_length": 200},
                        "options": {"description": "Array of option objects with name and price_difference", "required": true, "minItems": 2, "maxItems": 20}
                      },
                      "option_object": {
                        "name": {"description": "Option display name", "max_length": 40},
                        "price_difference": {"description": "Price difference compared to base option (default: 0, can be negative)", "type": "number"}
                      },
                      "examples": [
                        {
                          "type": "Option Dropdown",
                          "label": "Color",
                          "instructions": "Select your preferred color",
                          "options": [
                            {"name": "Red", "price_difference": 0},
                            {"name": "Blue", "price_difference": 0},
                            {"name": "Gold (Premium)", "price_difference": 5.00}
                          ]
                        },
                        {
                          "type": "Option Dropdown",
                          "label": "Size",
                          "options": [
                            {"name": "Small (3 inch)", "price_difference": 0},
                            {"name": "Medium (5 inch)", "price_difference": 3.00},
                            {"name": "Large (8 inch)", "price_difference": 7.50}
                          ]
                        }
                      ]
                    },
                    "number_customizations": {
                      "type": "array of Number customization objects",
                      "minItems": 0,
                      "maxItems": 15,
                      "description": "Numeric input fields",
                      "field_structure": {
                        "type": {"value": "Number", "required": true},
                        "label": {"description": "What number represents (Quantity, Year, Age, etc.)", "required": true, "max_length": 100},
                        "instructions": {"description": "Helper text", "required": false, "max_length": 200},
                        "min_value": {"description": "Minimum allowed value", "required": true, "type": "integer"},
                        "max_value": {"description": "Maximum allowed value", "required": true, "type": "integer"},
                        "placeholder": {"description": "Pre-filled value or example", "required": false, "type": "integer"}
                      },
                      "examples": [
                        {
                          "type": "Number",
                          "label": "Year",
                          "instructions": "Enter a year (e.g., 2025)",
                          "min_value": 1900,
                          "max_value": 2100,
                          "placeholder": 2025
                        }
                      ]
                    },
                    "image_customizations": {
                      "type": "array of Image customization objects",
                      "minItems": 0,
                      "maxItems": 15,
                      "description": "Image upload fields where customer's photo/logo will be placed on the product",
                      "preview_context": {
                        "size": "400x400 pixels (width x height)",
                        "coordinate_system": "Top-left corner is (0, 0), bottom-right is (400, 400)",
                        "origin": "Top-left corner"
                      },
                      "field_structure": {
                        "type": {"value": "Image", "required": true},
                        "label": {"description": "What to upload (Logo, Photo, Design, Face, Pet Photo, etc.)", "required": true, "max_length": 100},
                        "instructions": {"description": "Upload guidelines", "required": false, "max_length": 200},
                        "x": {"description": "X coordinate (0-400) from top-left where uploaded image will be placed", "required": true, "type": "integer", "minimum": 0, "maximum": 400},
                        "y": {"description": "Y coordinate (0-400) from top-left where uploaded image will be placed", "required": true, "type": "integer", "minimum": 0, "maximum": 400},
                        "width": {"description": "Width in pixels (10-400) of the image area", "required": true, "type": "integer", "minimum": 10, "maximum": 400},
                        "height": {"description": "Height in pixels (10-400) of the image area", "required": true, "type": "integer", "minimum": 10, "maximum": 400}
                      },
                      "examples": [
                        {
                          "type": "Image",
                          "label": "Face Photo",
                          "instructions": "Upload a clear face photo (will be placed in ornament)",
                          "x": 100,
                          "y": 100,
                          "width": 200,
                          "height": 200
                        },
                        {
                          "type": "Image",
                          "label": "Logo",
                          "instructions": "Upload your company logo (transparent background preferred)",
                          "x": 50,
                          "y": 50,
                          "width": 300,
                          "height": 300
                        }
                      ]
                    },
                    "customization_strategy": {
                      "from_variations": {
                        "rule": "Convert each variation to 'Option Dropdown' type",
                        "format": "Label: option1, option2 | Label2: optionA, optionB",
                        "conversion": [
                          "Parse variation label as customization label",
                          "Parse options and create option objects with price_difference: 0",
                          "Add instructions if helpful for customer"
                        ]
                      },
                      "from_personalization": {
                        "rule": "Analyze personalization text to extract custom fields",
                        "text_inputs": "Convert to 'Data' type (names, messages, dates as text)",
                        "numeric_inputs": "Convert to 'Number' type (years, quantities, ages)",
                        "image_uploads": "Convert to 'Image' type if mentions 'logo', 'photo', 'image'"
                      },
                      "smart_defaults": {
                        "Data": {
                          "min_characters": 1,
                          "max_characters": 100,
                          "lines_allowed": 1
                        },
                        "Number": {
                          "year_range": {"min_value": 1900, "max_value": 2100},
                          "quantity_range": {"min_value": 1, "max_value": 100},
                          "age_range": {"min_value": 1, "max_value": 120}
                        }
                      }
                    },
                    "exclude": [
                      "gift box", "preview", "paint color"
                    ]
                  },
                  "prohibited": [
                    "Digital/downloadable references",
                    "Brand names in title",
                    "Promotional language",
                    "External links",
                    "Guarantees in bullets",
                    "Special symbols: ™®©€…†‡£¥",
                    "Terms: eco-friendly, anti-bacterial, N/A, TBD",
                    "sale x%, discount, ..."
                  ]
                }
                """;
    }


}
