package com.example.backendseller.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OpenaiApiConfiguration {
    @Value("${openai.api.url}")
    private String url;

    @Bean
    public RestClient restClient() {
        return RestClient.builder()
                .baseUrl(url)
                .build();
    }
}
