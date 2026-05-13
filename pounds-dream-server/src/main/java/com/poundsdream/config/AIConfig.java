package com.poundsdream.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class AIConfig {

    @Value("${ai.api-key}")
    private String apiKey;

    @Value("${ai.base-url}")
    private String baseUrl;

    @Value("${ai.model}")
    private String model;
}
