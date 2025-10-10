package com.example.backendseller.dto.openai.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OpenAIRequest {
    private String model;
    private List<InputMessage> input;
    private TextFormat text;

    public static OpenAIRequest createProductRequest(String model, String systemMessage, String userMessage, String imageUrl) {
        return OpenAIRequest.builder()
                .model(model)
                .input(List.of(
                        InputMessage.builder()
                                .role("user")
                                .content(List.of(
                                        ContentItem.textContent(userMessage),
                                        ContentItem.imageContent(imageUrl)
                                ))
                                .build(),
                        InputMessage.builder()
                                .role("system")
                                .content(systemMessage)
                                .build()
                ))
                .text(createProductTextFormat())
                .build();
    }

    private static TextFormat createProductTextFormat() {
        // Schema cho Data type
        Map<String, Object> dataCustomizationSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of(
                                "type", "string",
                                "const", "Data"),
                        "label", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 100),
                        "instructions", Map.of(
                                "type", List.of("string", "null"),
                                "maxLength", 200
                        ),
                        "sample_text", Map.of(
                                "type", List.of("string", "null"),
                                "maxLength", 30
                        ),
                        "min_characters", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "default", 1),
                        "max_characters", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "maximum", 1000,
                                "default", 100),
                        "lines_allowed", Map.of(
                                "type", "integer",
                                "minimum", 1,
                                "maximum", 10,
                                "default", 1
                        )
                ),
                "required", List.of("type", "label", "instructions", "sample_text", "min_characters", "max_characters", "lines_allowed"),
                "additionalProperties", false
        );

        // Schema cho Option Dropdown
        Map<String, Object> optionSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "name", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 40),
                        "price_difference", Map.of(
                                "type", "number",
                                "default", 0.00)
                ),
                "required", List.of("name", "price_difference"),
                "additionalProperties", false
        );

        Map<String, Object> dropdownCustomizationSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of(
                                "type", "string",
                                "const", "Option Dropdown"),
                        "label", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 100),
                        "instructions", Map.of(
                                "type", List.of("string", "null"),
                                "maxLength", 200
                        ),
                        "options", Map.of(
                                "type", "array",
                                "items", optionSchema,
                                "minItems", 2,
                                "maxItems", 20
                        )
                ),
                "required", List.of("type", "label", "instructions", "options"),
                "additionalProperties", false
        );

        // Schema cho Number type
        Map<String, Object> numberCustomizationSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of(
                                "type", "string",
                                "const", "Number"),
                        "label", Map.of(
                                "type", "string",
                                "minLength", 1,
                                "maxLength", 100),
                        "instructions", Map.of(
                                "type", "string",
                                "maxLength", 200
                        ),
                        "min_value", Map.of("type", "integer"),
                        "max_value", Map.of("type", "integer"),
                        "placeholder", Map.of(
                                "type", "integer",
                                "maxLength", 30
                        )
                ),
                "required", List.of("type", "label", "instructions", "min_value", "max_value", "placeholder"),
                "additionalProperties", false
        );

        // Schema cho Image type
        Map<String, Object> imageCustomizationSchema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "type", Map.of("type", "string", "const", "Image"),
                        "label", Map.of("type", "string", "minLength", 1, "maxLength", 100),
                        "instructions", Map.of("type", List.of("string", "null"), "maxLength", 200),
                        "x", Map.of(
                                "type", "integer",
                                "minimum", 0,
                                "maximum", 400,
                                "description", "X coordinate from top-left corner"
                        ),
                        "y", Map.of(
                                "type", "integer",
                                "minimum", 0,
                                "maximum", 400,
                                "description", "Y coordinate from top-left corner"
                        ),
                        "width", Map.of(
                                "type", "integer",
                                "minimum", 10,
                                "maximum", 400,
                                "description", "Width of image area in pixels"
                        ),
                        "height", Map.of(
                                "type", "integer",
                                "minimum", 10,
                                "maximum", 400,
                                "description", "Height of image area in pixels"
                        )
                ),
                "required", List.of("type", "label", "instructions", "x", "y", "width", "height"),
                "additionalProperties", false
        );

        // Main schema properties
        Map<String, Object> schemaProperties = new LinkedHashMap<>();
        schemaProperties.put("title", Map.of("type", "string", "maxLength", 200));
        schemaProperties.put("description", Map.of("type", "string", "minLength", 50));
        schemaProperties.put("bullet_points", Map.of(
                "type", "array",
                "items", Map.of("type", "string", "minLength", 10, "maxLength", 255),
                "minItems", 5,
                "maxItems", 5
        ));
        schemaProperties.put("shape", Map.of("type", "string"));
        schemaProperties.put("material", Map.of(
                "type", "array",
                "items", Map.of(
                        "type", "string",
                        "enum", List.of("wood", "acrylic", "ceramic", "glass")
                ),
                "minItems", 1,
                "maxItems", 3
        ));
        schemaProperties.put("tags", Map.of("type", "string", "minLength", 300, "maxLength", 500));
        schemaProperties.put("data_customizations", Map.of(
                "type", "array",
                "items", dataCustomizationSchema,
                "minItems", 0,
                "maxItems", 15
        ));
        schemaProperties.put("dropdown_customizations", Map.of(
                "type", "array",
                "items", dropdownCustomizationSchema,
                "minItems", 0,
                "maxItems", 15
        ));
        schemaProperties.put("number_customizations", Map.of(
                "type", "array",
                "items", numberCustomizationSchema,
                "minItems", 0,
                "maxItems", 15
        ));
        schemaProperties.put("image_customizations", Map.of(
                "type", "array",
                "items", imageCustomizationSchema,
                "minItems", 0,
                "maxItems", 15
        ));
        schemaProperties.put("target_audience", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "minItems", 1,
                "maxItems", 5
        ));
        schemaProperties.put("occasion", Map.of(
                "type", "array",
                "items", Map.of("type", "string"),
                "minItems", 1,
                "maxItems", 5
        ));
        schemaProperties.put("item_dimensions", Map.of(
                "type", "object",
                "properties", Map.of(
                        "item_depth_front_to_back", Map.of("type", "number", "default", 0.1),
                        "item_height_floor_to_top", Map.of("type", "number", "default", 4),
                        "item_width_side_to_side", Map.of("type", "number", "default", 4),
                        "unit", Map.of("type", "string", "const", "Inches")
                ),
                "required", List.of("item_depth_front_to_back", "item_height_floor_to_top", "item_width_side_to_side", "unit"),
                "additionalProperties", false
        ));

        // Main schema
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", schemaProperties,
                "required", List.of("title", "description", "bullet_points", "shape", "material", "tags", "data_customizations", "dropdown_customizations", "number_customizations", "image_customizations", "target_audience", "occasion", "item_dimensions"),
                "additionalProperties", false
        );

        return TextFormat.builder()
                .format(Format.builder()
                        .type("json_schema")
                        .name("amazon_product_listing")
                        .schema(schema)
                        .strict(true) // ✅ Strict mode được hỗ trợ
                        .build())
                .build();
    }

}