package org.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "credentials")
public class Credential {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "hashed_password", nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Relația One-to-Many cu rolurile (se șterg automat dacă se șterge contul)
    // Folosim EAGER pentru că atunci când extragem userul pentru autentificare, avem mereu nevoie și de rolurile lui.
    @OneToMany(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<UserRole> roles = new HashSet<>();

    @OneToOne(mappedBy = "credential", cascade = CascadeType.ALL, orphanRemoval = true)
    private CredentialVerification verificationData;

    public Credential() {}

    public Credential(String email, String passwordHash, AccountStatus status) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.status = status;
    }

    // Helper pentru a adăuga roluri noi
    public void addRole(String roleName) {
        UserRole role = new UserRole(this, roleName);
        this.roles.add(role);
    }

    // --- Getters & Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AccountStatus getStatus() { return status; }
    public void setStatus(AccountStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<UserRole> getRoles() { return roles; }
    public void setRoles(Set<UserRole> roles) { this.roles = roles; }

    public CredentialVerification getVerificationData() { return verificationData; }

    // ne asiguram ca sunt asociate
    public void setVerificationData(CredentialVerification verificationData) {
        this.verificationData = verificationData;
        verificationData.setCredential(this);
    }
}