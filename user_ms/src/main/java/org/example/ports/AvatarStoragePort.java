package org.example.ports;

import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

public interface AvatarStoragePort {
    String uploadAvatar(UUID userId, MultipartFile file);
    void deleteAvatarByUrl(String avatarUrl);
}