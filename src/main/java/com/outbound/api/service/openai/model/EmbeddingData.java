package com.outbound.api.service.openai.model;

import java.util.List;

public class EmbeddingData {
    private List<Double> embedding;

    public List<Double> getEmbedding() {
        return embedding;
    }

    public void setEmbedding(List<Double> embedding) {
        this.embedding = embedding;
    }
}