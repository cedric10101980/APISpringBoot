package com.outbound.api.service;

import com.outbound.api.model.EmailRequest;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@Service
public class LLMQueryService {
    private static final Logger logger = LoggerFactory.getLogger(LLMQueryService.class);


    @Value("${query.url}")
    private String queryUrl;

    private WebClient webClient;

    private final WebClient.Builder webClientBuilder;

    @PostConstruct
    public void init() {
        this.webClient = webClientBuilder.baseUrl(queryUrl).build();
    }

    @Autowired
    public LLMQueryService(WebClient.Builder webClientBuilder) {
        this.webClientBuilder = webClientBuilder;
    }

    public Mono<ResponseEntity<String>> query(String query, String model) {
        return webClient.post()
                .uri("/querybot")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("query", query, "model", model))
                .retrieve()
                .bodyToMono(String.class)
                .doOnSubscribe(subscription -> logger.info("Subscription started for query in Service"))
                .doOnNext(response -> logger.info("Received response: {}", response))
                .doOnError(error -> logger.error("Error occurred during query: ", error))
                .doOnTerminate(() -> logger.info("Query process terminated"))
                .map(body -> new ResponseEntity<>(body, HttpStatus.OK))
                .onErrorResume(error -> {
                    logger.error("Exception caught in query method: ", error);
                    return Mono.just(new ResponseEntity<>("Error occurred during query", HttpStatus.INTERNAL_SERVER_ERROR));
                });
    }

    public Mono<ResponseEntity<String>> queryPrompt(String promptId, String campaignName, String contactId, String model) {
        return webClient.post()
                .uri("/querybotwithprompt")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("prompt", promptId, "campaignName", campaignName, "contactid", contactId, "model", model))
                .retrieve()
                .bodyToMono(String.class)
                .map(body -> new ResponseEntity<>(body, HttpStatus.OK));
    }

    public Mono<ResponseEntity<String>> sendEmail(EmailRequest request) {
        Mono<String> emailSendingProcess = webClient.post()
                .uri("/send-email")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("emailAddress", request.getEmailAddress(), "campaignName", request.getCampaignName(), "name", request.getName(), "bestTimeToContact", request.getBestTimeToCall()))
                .retrieve()
                .bodyToMono(String.class);

        // Start the email sending process in the background
        emailSendingProcess.subscribe();

        // Immediately return a response
        return Mono.just(new ResponseEntity<>("Email will be sent using LLM", HttpStatus.OK));
    }
}