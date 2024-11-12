package com.outbound.api.controller;

import com.outbound.api.domain.CannedPrompt;
import com.outbound.api.repository.PromptDataRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "Prompt Data", description = "Stores and retrieves PromptData")
@RequestMapping("/api/promptData")
public class PromptDataController {
    private final PromptDataRepository promptDataRepository;

    public PromptDataController(PromptDataRepository promptDataRepository) {
        this.promptDataRepository = promptDataRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new PromptData", description = "This operation creates a new PromptData document and saves it to MongoDB")
    public ResponseEntity<CannedPrompt> createPromptData(@RequestBody CannedPrompt promptData) {
        CannedPrompt savedPromptData = promptDataRepository.save(promptData);
        return new ResponseEntity<>(savedPromptData, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "Get all PromptData", description = "This operation retrieves all PromptData from MongoDB")
    public ResponseEntity<List<CannedPrompt>> getAllPromptData() {
        List<CannedPrompt> allPromptData = promptDataRepository.findAll();
        return new ResponseEntity<>(allPromptData, HttpStatus.OK);
    }

    @Operation(summary = "Delete PromptData by name", description = "This operation retrieves a PromptData by name from MongoDB")
    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deletePromptData(@PathVariable String name) {
        promptDataRepository.deleteById(name);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}