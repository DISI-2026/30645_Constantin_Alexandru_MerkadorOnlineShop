package org.example.services.adapters;

import org.example.ports.AvatarStoragePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Profile("!prod") // ca sa ii spunem lui Spring sa foloseasca aceasta clasa doar dacă nu suntem pe productie
public class LocalAvatarStorageAdapter implements AvatarStoragePort {

    private final Path storageDirectory = Paths.get("uploads/avatars");

    public LocalAvatarStorageAdapter() {
        try {
            Files.createDirectories(storageDirectory);
        } catch (IOException e) {
            throw new RuntimeException("Avatar directory could not be created: ", e);
        }
    }

    @Override
    public String uploadAvatar(UUID userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Empty file is not allowed");
        }
        try {
            String filename = userId.toString() + "_" + file.getOriginalFilename();
            Path destinationFile = storageDirectory.resolve(filename).normalize().toAbsolutePath();

            file.transferTo(destinationFile);

            // Returnăm o cale falsă/relativă pe care Frontend-ul o poate accesa
            return "/api/users/uploads/avatars/" + filename;
        } catch (IOException e) {
            throw new RuntimeException("Could not save avatar: ", e);
        }
    }
}