package com.outbound.api.service.openai;

import com.outbound.api.config.ImageGeneratorConfig;
import com.outbound.api.dto.AIImageDataDTO;
import com.outbound.api.dto.AIImageResponseDTO;
import com.outbound.api.service.ImageGeneratorClient;
import com.outbound.api.service.openai.model.*;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Service
public class OpenAIService {

    private final Logger logger = LoggerFactory.getLogger(OpenAIService.class);

    private static final String OPENAI_API_URL = "https://api.openai.com";

    private final ImageGeneratorClient imageGeneratorClient;

    //@Value("${openai.api.key}")
   // private String OPENAI_API_KEY;

    private WebClient webClient;

    private final String apiKey;

    @Autowired
    public OpenAIService(ImageGeneratorConfig imageGeneratorConfig, ImageGeneratorClient imageGeneratorClient) {
        this.apiKey = imageGeneratorConfig.getApiKey();
        this.imageGeneratorClient = imageGeneratorClient;
    }

    @PostConstruct
    void init() {
        var client = HttpClient.create().responseTimeout(Duration.ofSeconds(45));
        this.webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(client))
                .baseUrl(OPENAI_API_URL)
                .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .filter(logRequest())
                .build();
    }

    private ExchangeFilterFunction logRequest() {
        return (clientRequest, next) -> {
            logger.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> logger.info("{}={}", name, value)));
            return next.exchange(clientRequest);
        };
    }

    public Mono<Boolean> moderate(List<ChatCompletionMessage> messages) {
        return Flux.fromIterable(messages)
                .flatMap(this::sendModerationRequest)
                .collectList()
                .map(moderationResponses -> {
                    boolean hasFlaggedContent = moderationResponses.stream()
                            .anyMatch(response -> response.getResults().get(0).isFlagged());
                    return !hasFlaggedContent;
                });
    }

    @RegisterReflectionForBinding({ModerationRequest.class, ModerationResponse.class})
    private Mono<ModerationResponse> sendModerationRequest(ChatCompletionMessage message) {
        logger.debug("Sending moderation request for message: {}", message.getContent());
        return webClient.post()
                .uri("/v1/moderations")
                .bodyValue(new ModerationRequest(message.getContent()))
                .retrieve()
                .bodyToMono(ModerationResponse.class);
    }

    @RegisterReflectionForBinding(EmbeddingResponse.class)
    public Mono<List<Double>> createEmbedding(String text) {
        logger.debug("Creating embedding for text: {}", text);

        Map<String, Object> body = Map.of(
                "model", "text-embedding-ada-002",
                "input", text
        );

        return webClient.post()
                .uri("/v1/embeddings")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(EmbeddingResponse.class)
                .map(EmbeddingResponse::getEmbedding);
    }

    public Mono<String> processFileAndGenerateCompletion(FilePart file, String message) {
        return readFile(file)
                .map(fileContent -> message + "(data) = [ " + fileContent + " ]")
                .flatMap(fileContent -> generateCompletionStream(fileContent).reduce("", (message1, message2) -> (String) message1 + (String) message2));
    }

    @RegisterReflectionForBinding({ChatCompletionChunkResponse.class})
    public Flux<String> generateCompletionStream(String message) {
        ChatCompletionMessage chatGPTMessage = new ChatCompletionMessage(ChatCompletionMessage.Role.USER, message);
        logger.debug("Generating completion for message: {}", message);

        return webClient
                .post()
                .uri("/v1/chat/completions")
                .bodyValue(Map.of(
                        "model", "gpt-4-0125-preview",
                        "messages", Collections.singletonList(chatGPTMessage),
                        "stream", true
                ))
                .retrieve()
                .bodyToFlux(ChatCompletionChunkResponse.class)
                .onErrorResume(error -> {

                    // The stream terminates with a `[DONE]` message, which causes a serialization error
                    // Ignore this error and return an empty stream instead
                    if (error.getMessage().contains("JsonToken.START_ARRAY")) {
                        return Flux.empty();
                    }

                    // If the error is not caused by the `[DONE]` message, return the error
                    else {
                        return Flux.error(error);
                    }
                })
                .filter(response -> {
                    var content = response.getChoices().get(0).getDelta().getContent();
                    logger.debug("Received chunk: {}", content);
                    return content != null && !content.equals("\n\n");
                })
                .map(response -> response.getChoices().get(0).getDelta().getContent());
    }

    public Mono<String> readFile(FilePart csvFile) {
            if (csvFile == null) {
                return Mono.just("No image provided or image is empty");
            }

        return DataBufferUtils.join(csvFile.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                });
    }

    public Flux<String> generateImage(String description) {
        GenerateImageResponse response = imageGeneratorClient.generateImage(createGenerateImageRequest(description));

        if (!response.getData().isEmpty()) {
            GeneratedImage firstImage = response.getData().get(0);

            String url = firstImage.getUrl();
            logger.info("Generated image with URL: {}", url);
            return url != null ? Flux.just(url) : Flux.empty();
        } else {
            return Flux.empty();
        }
    }

    private GenerateImageRequest createGenerateImageRequest(String description) {
        GenerateImageRequest request = new GenerateImageRequest();
        request.setPrompt(description);
        // Set other fields as necessary
        // For example:
        request.setSize("1024x1024");
        request.setNumImages(1);
        return request;
    }

    public Flux<AIImageDataDTO> generateImagenonsense(String description) {


        Map<String, Object> body = Map.of(
                "model", "dall-e-3",
                "prompt", description,
                "n", 1,
                "size", "1024x1024"
        );

        return webClient.post()
                .uri("/v1/images/generations")
                .bodyValue(body)
                .retrieve()
                .bodyToFlux(AIImageResponseDTO.class)
                .onErrorResume(error -> {

                    // The stream terminates with a `[DONE]` message, which causes a serialization error
                    // Ignore this error and return an empty stream instead
                    if (error.getMessage().contains("JsonToken.START_ARRAY")) {
                        return Flux.empty();
                    }

                    // If the error is not caused by the `[DONE]` message, return the error
                    else {
                        return Flux.error(error);
                    }
                })
                .filter(response -> {
                    AIImageDataDTO content = response.getData().get(0);
                    logger.debug("Received chunk: {}", content.getUrl());
                    return !content.getUrl().equals("\n\n");
                })
                .map(response -> response.getData().get(0));
    }

}
