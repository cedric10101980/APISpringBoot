package com.outbound.api.service;

import com.outbound.api.dto.DBImageResponseDTO;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import org.springframework.core.io.buffer.DataBufferUtils;
import java.io.InputStream;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

@Service
public class ImageUploadService {

    @Autowired
    private GridFsTemplate gridFsTemplate;

    public Mono<ResponseEntity<DBImageResponseDTO>> uploadImage(Mono<FilePart> imageMono, Optional<String> contactID) {
        return imageMono.flatMap(imageFile -> {
            if (imageFile == null) {
                return Mono.just(new ResponseEntity<>(new DBImageResponseDTO("", 0, "No image provided or image is empty"), HttpStatus.BAD_REQUEST));
            }

            String contentType = Objects.requireNonNull(imageFile.headers().getContentType()).toString();
            if (!contentType.startsWith("image/")) {
                return Mono.just(new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), 0, "File provided is not an image"), HttpStatus.BAD_REQUEST));
            }

            return DataBufferUtils.join(imageFile.content()).map(dataBuffer -> {
                try (InputStream inputStream = dataBuffer.asInputStream()) {
                    // Use contactID if it is present
                    String filename = contactID.orElseGet(imageFile::filename);
                    // Check if image with the same name already exists
                    GridFSFile existingFile = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(filename)));
                    if (existingFile != null) {
                        return new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), 0, "Image with the same name already exists. Delete it first"), HttpStatus.CONFLICT);
                    }

                    gridFsTemplate.store(inputStream, filename, Objects.requireNonNull(imageFile.headers().getContentType()).toString());
                    return new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), dataBuffer.readableByteCount(), "OK"), HttpStatus.OK);
                } catch (IOException e) {
                    return new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), 0, "Failed to upload image: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                } catch (Exception e) {
                    return new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), 0, "Unexpected error occurred: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR);
                }
            });
        }).doOnError(e -> System.out.println(Arrays.toString(e.getStackTrace())));
    }
}
