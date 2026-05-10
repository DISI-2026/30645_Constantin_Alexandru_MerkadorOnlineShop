package org.example.services.adapters;

import org.example.ports.LogoStoragePort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.UUID;

@Service
@Profile("!prod")
public class LocalLogoStorageAdapter implements LogoStoragePort {

    private final Path storageDirectory;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp"
    );

    public LocalLogoStorageAdapter(@Value("${storage.upload-root:./upload_data}") String uploadRoot) {
        this.storageDirectory = Paths.get(uploadRoot, "logos");
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Logo directory could not be created: ", e);
        }
    }

    @Override
    public String uploadLogo(UUID userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file is not allowed.");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Only JPG, PNG and WEBP images are allowed.");
        }

        String extension = getExtension(file.getOriginalFilename());
        String filename = userId.toString() + "_" + UUID.randomUUID() + extension;

        try {
            Path destinationFile = storageDirectory.resolve(filename).normalize().toAbsolutePath();
            file.transferTo(destinationFile);
            return "/api/users/uploads/logos/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not save logo: ", e);
        }
    }

    @Override
    public void deleteLogoByUrl(String logoUrl) {
        if (logoUrl == null) return;

        String filename = logoUrl.substring(logoUrl.lastIndexOf("/") + 1);

        try {
            Path filePath = storageDirectory.resolve(filename).normalize().toAbsolutePath();
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new RuntimeException("Could not delete logo: ", e);
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