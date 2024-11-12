package com.outbound.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AIImageDataDTO {
    private String revised_prompt;
    private String url;

    // getters and setters
}
