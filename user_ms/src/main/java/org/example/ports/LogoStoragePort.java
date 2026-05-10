package org.example.ports;

import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface LogoStoragePort {
    String uploadLogo(UUID userId, MultipartFile file);
    void deleteLogoByUrl(String avatarUrl);
}