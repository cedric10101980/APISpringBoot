package com.outbound.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIImageResponseDTO {
    private long created;
    private List<AIImageDataDTO> data;

    // getters and setters
}


