package com.example.backendseller.dto.openai.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "title", Map.of("type", "string"),
                        "description", Map.of("type", "string"),
                        "bullet_points", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "shape", Map.of("type", "string"),
                        "material", Map.of(
                                "type", "array",
                                "items", Map.of("type", "string")
                        ),
                        "tags", Map.of("type", "string"),
                        "customizations", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "type", Map.of("type", "string"),
                                                "label", Map.of("type", "string"),
                                                "options", Map.of(
                                                        "type", "array",
                                                        "items", Map.of("type", "string")
                                                )
                                        ),
                                        // ✅ chỉ bắt buộc "type" và "label", "options" là optional
                                        "anyOf", List.of(
                                                // Variant 1: Không có options (optional)
                                                Map.of(
                                                        "required", List.of("type", "label"),
                                                        "additionalProperties", false
                                                ),
                                                // Variant 2: Có options
                                                Map.of(
                                                        "required", List.of("type", "label", "options"),
                                                        "additionalProperties", false
                                                )
                                        ),
                                        "additionalProperties", false
                                )
                        )
                ),
                "required", List.of(
                        "title", "description", "bullet_points",
                        "shape", "material", "tags", "customizations"
                ),
                "additionalProperties", false
        );

        return TextFormat.builder()
                .format(Format.builder()
                        .type("json_schema")
                        .name("product_detail_single")
                        .schema(schema)
                        .strict(true)
                        .build())
                .build();
    }

}