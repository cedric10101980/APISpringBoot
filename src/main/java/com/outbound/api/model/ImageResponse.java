package com.outbound.api.model;

import lombok.Data;

@Data
public class ImageResponse {
    private String imageUrl;
    private String prompt;

    // getters and setters
}