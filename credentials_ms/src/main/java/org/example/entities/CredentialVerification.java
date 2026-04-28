package org.example.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "credentials_verifications")
public class CredentialVerification {

    @Id
    private UUID id; // Nu are @GeneratedValue pentru că împarte ID-ul cu Credential

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId // Asta face ca 'id' să fie și Primary Key și Foreign Key către 'credentials'
    @JoinColumn(name = "user_id")
    private Credential credential;

    @Column(name = "verification_code", length = 6)
    private String verificationCode;

    @Column(name = "code_expires_at")
    private LocalDateTime codeExpiresAt;

    @Column(name = "pending_email")
    private String pendingEmail;

    public CredentialVerification() {}

    public CredentialVerification(Credential credential) {
        this.credential = credential;
    }

    // O metodă helper pentru a genera și seta un cod nou (OTP) valabil 15 minute
    public void generateNewCode() {
        // Generează un număr random de 6 cifre (ex: 049281)
        this.verificationCode = String.format("%06d", new java.util.Random().nextInt(999999));
        this.codeExpiresAt = LocalDateTime.now().plusMinutes(15);
    }

    public void clearCode() {
        this.verificationCode = null;
        this.codeExpiresAt = null;
    }

    // --- Getters și Setters ---
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Credential getCredential() { return credential; }
    public void setCredential(Credential credential) { this.credential = credential; }
    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }
    public LocalDateTime getCodeExpiresAt() { return codeExpiresAt; }
    public void setCodeExpiresAt(LocalDateTime codeExpiresAt) { this.codeExpiresAt = codeExpiresAt; }
    public String getPendingEmail() { return pendingEmail; }
    public void setPendingEmail(String pendingEmail) { this.pendingEmail = pendingEmail; }
}