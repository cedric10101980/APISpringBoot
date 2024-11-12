package com.outbound.api.controller;

import com.outbound.api.service.FileStorageService;
import com.outbound.api.dto.DBImageResponseDTO;
import com.outbound.api.dto.ResponseMessage;
import com.outbound.api.model.FileInfo;
import com.outbound.api.service.ImageUploadService;
import com.mongodb.client.gridfs.model.GridFSFile;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.util.StreamUtils;

import java.util.Base64;

@Hidden
@CrossOrigin
@RestController
@Tag(name = "Image Uploader Retriever", description = "The Image Uploader and Retriever API")
public class ImageUploadController {
    private static final Logger logger = LogManager.getLogger(ImageUploadController.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;

    @Autowired
    FileStorageService storageService;
    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping(value = "/uploadImageAsyncDB", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload an image to MongoDB asynchronously", description = "This method is used to upload an image to MongoDB asynchronously.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "No image provided or image is empty"),
            @ApiResponse(responseCode = "400", description = "File provided is not an image"),
            @ApiResponse(responseCode = "409", description = "Image with the same name already exists"),
            @ApiResponse(responseCode = "500", description = "Failed to upload image")
    })
    public Mono<ResponseEntity<DBImageResponseDTO>> uploadImageAsyncDB(@RequestPart("file") Mono<FilePart> imageMono, @RequestPart("contactId") String contactId) {
        return imageMono.flatMap(imageFile -> {
            try {
                return imageUploadService.uploadImage(imageMono, Optional.ofNullable(contactId));
            } catch (Exception e) {
                return Mono.just(new ResponseEntity<>(new DBImageResponseDTO(imageFile.filename(), 0, "Unexpected error occurred: " + e.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR));
            }
        }).doOnError(e -> System.out.println(Arrays.toString(e.getStackTrace())));
    }

    @GetMapping("/getImageFromDB/{filename}")
    @Operation(summary = "Retrieve an image from Chat GPT", description = "This method is used to retrieve an image from Chat GPT using Dall - E API.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<String> getImageFromDB(@PathVariable String filename) throws IOException {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(filename)));
        if (file == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        GridFsResource resource = gridFsTemplate.getResource(file);
        byte[] bytes = StreamUtils.copyToByteArray(resource.getInputStream());
        String encodedString = Base64.getEncoder().encodeToString(bytes);

        return new ResponseEntity<>(encodedString, HttpStatus.OK);
    }

    @DeleteMapping("/deleteImageFromDB/{filename}")
    @Operation(summary = "Delete an image from Service", description = "This method is used to delete an image from Service.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Image deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Image not found")
    })
    public ResponseEntity<String> deleteImageFromDB(@PathVariable String filename) {
        GridFSFile file = gridFsTemplate.findOne(new Query(Criteria.where("filename").is(filename)));
        if (file == null) {
            return new ResponseEntity<>("Image not found", HttpStatus.NOT_FOUND);
        }

        gridFsTemplate.delete(new Query(Criteria.where("filename").is(filename)));

        return new ResponseEntity<>("Image deleted successfully", HttpStatus.OK);
    }

    @Hidden
    @PostMapping(value = "/uploadFile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Upload a file", description = "This method is used to upload a file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request, file not provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<DBImageResponseDTO>> uploadFile(@RequestPart("file") Mono<FilePart> filePartMono) {
        if (filePartMono == null) {
            logger.error("Bad request, file not provided");

            return Mono.just(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
        }

        try {
            return storageService.save(filePartMono)
                    .map(fileInfo -> ResponseEntity.ok().body(fileInfo))
                    .onErrorReturn(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        } catch (Exception e) {
            logger.error("Internal server error", e);

            // Log the exception
            return Mono.just(new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR));
        }
    }

    @Hidden
    @GetMapping("/files")
    @Operation(summary = "Get list of files", description = "This method is used to get a list of all files.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Flux<FileInfo>> getListFiles() {
        try {
            Stream<FileInfo> fileInfoStream = storageService.loadAll().map(path -> {
                String filename = path.getFileName().toString();
                String url = UriComponentsBuilder.newInstance().path("/files/{filename}").buildAndExpand(filename).toUriString();
                return new FileInfo(filename, url);
            });

            Flux<FileInfo> fileInfosFlux = Flux.fromStream(fileInfoStream);

            return ResponseEntity.status(HttpStatus.OK).body(fileInfosFlux);
        } catch (Exception e) {
            // Log the exception
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Hidden
    @GetMapping("/getFile/{filename:.+}")
    @Operation(summary = "Get a file", description = "This method is used to get a specific file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved file"),
            @ApiResponse(responseCode = "400", description = "Bad request, filename not provided"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Flux<DataBuffer>> getFile(@PathVariable String filename) {
        if (filename == null || filename.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        try {
            Flux<DataBuffer> file = storageService.load(filename);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(file);
        } catch (Exception e) {
            // Log the exception
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Hidden
    @DeleteMapping("/files/{filename:.+}")
    @Operation(summary = "Delete a file", description = "This method is used to delete a specific file.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully deleted file"),
            @ApiResponse(responseCode = "400", description = "Bad request, filename not provided"),
            @ApiResponse(responseCode = "404", description = "Not found, file does not exist"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public Mono<ResponseEntity<ResponseMessage>> deleteFile(@PathVariable String filename) {
        if (filename == null || filename.isEmpty()) {
            return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ResponseMessage("Filename is required")));
        }

        try {
            boolean existed = storageService.delete(filename);

            if (existed) {
                String message = "Delete the file successfully: " + filename;
                return Mono.just(ResponseEntity.ok().body(new ResponseMessage(message)));
            }

            String message = "The file does not exist!";
            return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ResponseMessage(message)));
        } catch (Exception e) {
            String message = "Could not delete the file: " + filename + ". Error: " + e.getMessage();
            // Log the exception
            return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ResponseMessage(message)));
        }
    }
}