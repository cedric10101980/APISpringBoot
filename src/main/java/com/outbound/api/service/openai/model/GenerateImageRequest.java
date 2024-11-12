package com.outbound.api.service.openai.model;

import com.outbound.api.config.ValidSize;
import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;



/**
 * @author DennisBrysiuk
 */
@Data
public class GenerateImageRequest {

    @NotBlank
    private String prompt;

    @ValidSize
    private String size;

    @Min(1)
    @Max(10)
    @JsonProperty("num_images")
    private int numImages;

}
