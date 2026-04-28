package org.example.repositories;

import org.example.entities.Credential;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CredentialRepository extends JpaRepository<Credential, UUID>{
    Optional<Credential> findById(UUID id);
    Optional<Credential> findByEmail(String email);
}
