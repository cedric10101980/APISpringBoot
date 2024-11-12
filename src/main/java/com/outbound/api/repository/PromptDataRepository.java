package com.outbound.api.repository;

import com.outbound.api.domain.CannedPrompt;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PromptDataRepository extends MongoRepository<CannedPrompt, String> {
    CannedPrompt findByType(String type);
}