package com.example.backendseller.config;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GoogleConfig {

    @Bean
    public NetHttpTransport netHttpTransport() throws Exception {
        return GoogleNetHttpTransport.newTrustedTransport();
    }
}
