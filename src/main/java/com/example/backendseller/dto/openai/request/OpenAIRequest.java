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

    public static OpenAIRequest createProductRequest(String model, String systemMessage, String userMessage, String imageUrl, int numberOfResponse) {
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
                .text(createProductTextFormat(numberOfResponse))
                .build();
    }

    private static TextFormat createProductTextFormat(int numberOfResponse) {
        Map<String, Object> schema = Map.of(
                "type", "object",
                "properties", Map.of(
                        "product_details", Map.of(
                                "type", "array",
                                "items", Map.of(
                                        "type", "object",
                                        "properties", Map.of(
                                                "title", Map.of("type", "string"),
                                                "description", Map.of("type", "string"),
                                                "bullet_points", Map.of(
                                                        "type", "array",
                                                        "items", Map.of("type", "string")
                                                ),
                                                "shape", Map.of("type", "string"),
                                                "material", Map.of("type", "string"),
                                                "tags", Map.of("type", "string")
                                        ),
                                        "required", List.of("title", "description", "bullet_points", "shape", "material", "tags"),
                                        "additionalProperties", false
                                ),
                                "minItems", numberOfResponse,
                                "maxItems", numberOfResponse
                        )
                ),
                "required", List.of("product_details"),
                "additionalProperties", false
        );

        return TextFormat.builder()
                .format(Format.builder()
                        .type("json_schema")
                        .name("product_detail")
                        .schema(schema)
                        .strict(true)
                        .build())
                .build();
    }
}