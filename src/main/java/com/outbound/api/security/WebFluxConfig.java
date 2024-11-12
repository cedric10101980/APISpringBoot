package com.outbound.api.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.ResourceHandlerRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

@Configuration
public class WebFluxConfig implements WebFluxConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/public/.well-known/**")
                .addResourceLocations("classpath:/resources/public/.well-known/")
                .addResourceLocations("classpath:/resources/static/.well-known/");
    }
}