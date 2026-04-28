package org.example.entities;

import jakarta.persistence.*;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "credentials_roles")
public class UserRole {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "credential_id", nullable = false)
    private Credential credential;

    @Column(nullable = false)
    private String role; // Ex: "BUYER", "SELLER", "ADMIN"

    public UserRole() {}

    public UserRole(Credential credential, String role) {
        this.credential = credential;
        this.role = role;
    }

    // --- Getters & Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Credential getCredential() { return credential; }
    public void setCredential(Credential credential) { this.credential = credential; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}