package com.outbound.api.dto;

import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

@Data
public class ImageDTO {
    private String contactId;
    Mono<FilePart> imageMono;

}
