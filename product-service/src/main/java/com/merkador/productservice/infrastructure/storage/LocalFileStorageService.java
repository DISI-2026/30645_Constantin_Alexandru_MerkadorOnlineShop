package com.merkador.productservice.infrastructure.storage;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageService {

    private final Path storageDirectory;
    
    public LocalFileStorageService(@Value("${app.storage.upload-root:./upload_data}") String uploadRoot) {
        this.storageDirectory = Paths.get(uploadRoot, "products");
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Product images directory could not be created: ", e);
        }
    }

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public String saveProductImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Image file is required.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP images are allowed.");
        }

        String extension = getExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + extension;

        try {
            Path destinationFile = storageDirectory.resolve(filename).normalize().toAbsolutePath();

            file.transferTo(destinationFile);

            return "/uploads/products/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not save image file.", e);
        }
    }

    public void deleteProductImageByUrl(String imageUrl) {
        if (imageUrl == null) return;

        String filename = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);

        try {
            Path filePath = storageDirectory.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete image file.", e);
        }
    }

    private String getExtension(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return ".jpg";
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf('.')).toLowerCase();
        if (!Set.of(".jpg", ".jpeg", ".png", ".webp").contains(extension)) {
            throw new IllegalArgumentException("Invalid image extension.");
        }
        return extension;
    }
}