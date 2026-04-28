package org.example.repositories;

import org.example.entities.CredentialVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface CredentialVerificationRepository extends JpaRepository<CredentialVerification, UUID> {
}