package com.outbound.api.dto;

public class DBImageResponseDTO {
    private String imageName;
    private long imageSize;
    private String status;

    public DBImageResponseDTO(String imageName, long imageSize, String status) {
        this.imageName = imageName;
        this.imageSize = imageSize;
        this.status = status;
    }
    // Getters and setters
    // ...
    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public long getImageSize() {
        return imageSize;
    }

    public void setImageSize(long imageSize) {
        this.imageSize = imageSize;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // ...
}