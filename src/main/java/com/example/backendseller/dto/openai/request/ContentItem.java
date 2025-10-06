package com.example.backendseller.dto.openai.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ContentItem {
    private String type;
    private String text;

    @JsonProperty("image_url")
    private String imageUrl;

    public static ContentItem textContent(String text) {
        return ContentItem.builder()
                .type("input_text")
                .text(text)
                .build();
    }

    public static ContentItem imageContent(String imageUrl) {
        return ContentItem.builder()
                .type("input_image")
                .imageUrl(imageUrl)
                .build();
    }
}