package com.outbound.api.service.openai.model;

import lombok.Data;

import java.util.List;

/**
 * @author DennisBrysiuk
 */
@Data
public class GenerateImageResponse {

    private List<GeneratedImage> data;

}
