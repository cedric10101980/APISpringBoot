package com.outbound.api.service;

import com.outbound.api.dto.DBImageResponseDTO;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class FileStorageServiceImpl implements FileStorageService {
  private static final Logger logger = LogManager.getLogger(FileStorageServiceImpl.class);

  private final Path root = Paths.get("uploads");

  @Override
  public void init() {
    try {
      Files.createDirectories(root);
      logger.info("Folder for upload initialized successfully!");
    } catch (IOException e) {
      logger.error("Could not initialize folder for upload!", e);

      throw new RuntimeException("Could not initialize folder for upload!");
    }
  }

  @Override
  public Mono<DBImageResponseDTO> save(Mono<FilePart> filePartMono) {

    return filePartMono.doOnNext(fp -> System.out.println("Receiving File:" + fp.filename())).flatMap(filePart -> {
      String filename = filePart.filename();
      return filePart.transferTo(root.resolve(filename)).then(Mono.just(new DBImageResponseDTO(filePart.filename(), 0, "File uploaded successfully")));
    });
  }

  @Override
  public Flux<DataBuffer> load(String filename) {
    try {
      Path file = root.resolve(filename);
      Resource resource = new UrlResource(file.toUri());

      if (resource.exists() || resource.isReadable()) {
        return DataBufferUtils.read(resource, new DefaultDataBufferFactory(), 4096);
      } else {
        throw new RuntimeException("Could not read the file!");
      }
    } catch (MalformedURLException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }

  @Override
  public Stream<Path> loadAll() {
    try {
      return Files.walk(this.root, 1)
          .filter(path -> !path.equals(this.root))
          .map(this.root::relativize);
    } catch (IOException e) {
      throw new RuntimeException("Could not load the files!");
    }
  }

  @Override
  public boolean delete(String filename) {
    try {
      Path file = root.resolve(filename);
      return Files.deleteIfExists(file);
    } catch (IOException e) {
      throw new RuntimeException("Error: " + e.getMessage());
    }
  }
}
