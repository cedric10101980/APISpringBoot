package com.outbound.api;

import com.outbound.api.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableFeignClients
//@OpenAPIDefinition(info = @Info(title = "Backend Service APIs for Supporting OpenAI and LLM chat bot", version = "1.0", description = "Spring WebFlux application with OpenAPI communication with backend MongoDB and OpenAI services."))
public class AiImageServiceApplication implements CommandLineRunner {

    @Resource
    FileStorageService storageService;
    private static final Logger logger = LogManager.getLogger(AiImageServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AiImageServiceApplication.class, args);
    }

    @Bean
    public ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    @Override
    public void run(String... arg) throws Exception {
        try {
            storageService.init();
        } catch (Exception e) {
            logger.error("Error initializing storage service", e);
        }
    }
}


