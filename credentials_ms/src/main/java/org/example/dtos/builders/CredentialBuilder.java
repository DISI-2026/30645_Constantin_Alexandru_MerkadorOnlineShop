package org.example.dtos.builders;

import org.example.dtos.CredentialRespDTO;
import org.example.entities.Credential;
import org.example.entities.UserRole;

import java.util.List;
import java.util.stream.Collectors;

public class CredentialBuilder {

    public static CredentialRespDTO toCredentialRespDTO(Credential credential) {
        List<String> roleNames = credential.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        return new CredentialRespDTO(
                credential.getId(),
                credential.getEmail(),
                credential.getStatus().name(),
                roleNames,
                credential.getCreatedAt(),
                credential.getUpdatedAt()
        );
    }
}