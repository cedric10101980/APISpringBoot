package com.outbound.api.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author DennisBrysiuk
 */
@Configuration
public class ImageGeneratorConfig {

    @Value("${openai.api.key}")
    private String apiKey;

    @Bean
    public RequestInterceptor apiKeyInterceptor() {
        return template -> template.header("Authorization", "Bearer " + apiKey);
    }

    public String getApiKey() {
        return apiKey;
    }
}
