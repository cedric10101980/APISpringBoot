package com.outbound.api.controller;

import com.outbound.api.service.LLMQueryService;
import com.outbound.api.model.EmailRequest;
import com.outbound.api.model.ImageResponse;
import com.outbound.api.service.openai.OpenAIService;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/openapi")
@Tag(name = "Open AI LLM APIs", description = "APIs to interact with OpenAI's [GPT4](https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4) or [GPT3.5](https://platform.openai.com/docs/models/gpt-3-5-turbo) models")
public class AAOpenAIController {
    private static final Logger logger = LoggerFactory.getLogger(AAOpenAIController.class);
    private final OpenAIService openAIService;
    private final LLMQueryService llmQueryService;

    @Autowired
    public AAOpenAIController(OpenAIService openAIService, LLMQueryService llmQueryService) {
        this.openAIService = openAIService;
        this.llmQueryService = llmQueryService;
    }

    @Schema(description = "Model for querying the chatbot")
    public static class QueryModel {
        @Schema(description = "The query string to be sent to the chatbot. This field is mandatory.")
        @NotNull(message = "query is mandatory")
        public String query;
        @Nullable
        @Schema(description = """
            This Field is optional and uses "gpt-4-0125-preview" model if not specified or u can provide any of these\s
            [GPT 4](https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4) or GPT3.5 [GPT3.5](https://platform.openai.com/docs/models/gpt-3-5-turbo) models""")
        public String model;
    }

    @Schema(description = "Model for querying the chatbot with Prompt")
    public static class PromptModel {
        @Schema(description = "The promptId to be sent to the chatbot. This field is mandatory.")
        @NotNull(message = "promptId is mandatory")
        public String promptId;

        @Schema(description = "The campaignName to be sent to the chatbot. This field is mandatory.")
        @NotNull(message = "campaignName is mandatory")
        public String campaignName;

        @Schema(description = "The contactId to be sent to the chatbot. This field is mandatory.")
        @NotNull(message = "contactId is mandatory")
        public String contactId;

        @Nullable
        @Schema(description = """
            This Field is optional and uses "gpt-4-0125-preview" model if not specified or u can provide any of these\s
            [GPT 4](https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4) or GPT3.5 [GPT3.5](https://platform.openai.com/docs/models/gpt-3-5-turbo) models""")
        public String model;
    }

    @PostMapping("/queryChatBot")
    @Operation(summary = "Query Trained ChatBot", description = """
            This endpoint allows querying the chatbot with a provided query string. and model selection based on the provided models of Open AI\s
            Models - OpenAI API
            >
            [GPT 4](https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4) or GPT3.5 [GPT3.5](https://platform.openai.com/docs/models/gpt-3-5-turbo) models""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The query and model to use", required = true, content = @Content(schema = @Schema(implementation = QueryModel.class)))
    public Mono<ResponseEntity<String>> query(@RequestBody Map<String, String> request) {
        String model = request.getOrDefault("model", "gpt-4-0125-preview"); // default model
        logger.info("In Query Model selected: {}", model);
        logger.info("Request parameters: promptId={}, campaignName={}, contactId={}", request.getOrDefault("query", ""));

        return llmQueryService.query(request.get("query"), model)
                .doOnSubscribe(subscription -> logger.info("Subscription started for Query"))
                .doOnNext(response -> logger.info("Received response: {}", response))
                .doOnError(error -> logger.error("Error occurred: ", error))
                .doOnTerminate(() -> logger.info("Query prompt process terminated"));
    }

    @PostMapping("/queryPrompt")
    @Operation(summary = "Query Trained ChatBot", description = """
            This endpoint allows querying the chatbot with a provided prompt id. and model selection based on the provided models of Open AI\s
            Models - OpenAI API
            >
            [GPT 4](https://platform.openai.com/docs/models/gpt-4-turbo-and-gpt-4) or GPT3.5 [GPT3.5](https://platform.openai.com/docs/models/gpt-3-5-turbo) models""")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The query and model to use", required = true, content = @Content(schema = @Schema(implementation = PromptModel.class)))
    public Mono<ResponseEntity<String>> queryPrompt(@RequestBody Map<String, String> request) {
        String model = request.getOrDefault("model", "gpt-4-0125-preview"); // default model
        logger.info("In Query Prompt Model selected: {}", model);
        logger.info("Request parameters: promptId={}, campaignName={}, contactId={}", request.get("promptId"), request.get("campaignName"), request.get("contactId"));

        return llmQueryService.queryPrompt(request.get("promptId"), request.get("campaignName"), request.get("contactId"), model)
                .doOnSubscribe(subscription -> logger.info("Subscription started"))
                .doOnNext(response -> logger.info("Received response: {}", response))
                .doOnError(error -> logger.error("Error occurred: ", error))
                .doOnTerminate(() -> logger.info("Query prompt process terminated"));
    }

    @Hidden
    @PostMapping("/generate-probabilities/{campaignName}")
    @Operation(summary = "Generate probabilities by description",
            description = "This operation generates probabilities for contacts using history data by using OpenAI API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<String> generateCompletion(@PathVariable String campaignName) {

        return openAIService.generateCompletionStream(campaignName);
    }

    /**
     * Sends an email based on the provided request.
     *
     * @param emailRequest The email request containing the necessary information to send an email.
     * @return A Mono wrapping a ResponseEntity with a String message indicating the result of the operation.
     */
    @Operation(summary = "Send an email", description = "This endpoint sends an email based on the provided request.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email sent successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request, invalid input provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred while trying to send the email")
    })
    @PostMapping("/send-email")
    public Mono<ResponseEntity<String>> sendEmail(@RequestBody EmailRequest emailRequest) {
        String model = "gpt-4-0125-preview";
        return  llmQueryService.sendEmail(emailRequest);
    }

    @Hidden
    @PostMapping(value = "/generate-probabilities-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process CSV file with contact call records",
            description = "This operation processes a CSV file and generates probabilities for contacts using history data by using OpenAI API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successful operation"),
            @ApiResponse(responseCode = "400", description = "Invalid input, file is empty or not a CSV file"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<String>> processCSV(@Parameter(description = "The message to process", required = true) @RequestPart String message,
                                                   @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The files to upload", required = true, content = @Content(schema = @Schema(type = "string", format = "binary")))
                                                   @RequestPart("file") Mono<FilePart> imageMono) {

        return imageMono.flatMap(file -> {
            // Check if the file is not empty
            if (file == null) {
                return Mono.just(new ResponseEntity<>("The file is empty", HttpStatus.BAD_REQUEST));
            }

            // Check if the file is a CSV file
            if (!file.filename().toLowerCase().endsWith(".csv")) {
                return Mono.just(new ResponseEntity<>("The file is not a CSV file", HttpStatus.BAD_REQUEST));
            }

            return openAIService.processFileAndGenerateCompletion(file, message)
                    .flatMap(response -> {
                        logger.info("Processed message: " + message + " and file: " + file.filename());

                        // For now, just return a placeholder response
                        return Mono.just(new ResponseEntity<>(response, HttpStatus.OK));
                    });
        });
    }

    @Hidden
    @PostMapping("/generate-image")
    @Operation(summary = "Generate image by description",
            description = "This operation generates an image by its description using OpenAI API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image generated"),
            @ApiResponse(responseCode = "404", description = "Image not generated"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Flux<ImageResponse> generateImageByDescription(@RequestBody String description) {
        return openAIService.generateImage(description).map(urlResponse -> {
            ImageResponse response = new ImageResponse();
            response.setImageUrl(urlResponse);
            response.setPrompt(description);
            return response;
        });
    }
}