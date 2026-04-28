package org.example.services;

import jakarta.validation.Valid;
import org.example.dtos.AuthDTO;
import org.example.dtos.LoginReqDTO;
import org.example.dtos.RegisterReqDTO;
import org.example.dtos.CredentialRespDTO;
import org.example.dtos.builders.CredentialBuilder;
import org.example.entities.*;
import org.example.handlers.exceptions.model.AccountNotActiveException;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.repositories.CredentialRepository;
import org.example.repositories.CredentialVerificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CredentialService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);

    private final CredentialRepository credentialRepository;
    private final UserSyncProducer userSyncProducer;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordResetService passwordResetService;
    private final CredentialVerificationRepository credentialVerificationRepository;
    private final EmailService emailService;

    @Autowired
    public CredentialService(CredentialRepository credentialRepository,
                             UserSyncProducer userSyncProducer,
                             JwtService jwtService,
                             RefreshTokenService refreshTokenService,
                             PasswordResetService passwordResetService,
                             CredentialVerificationRepository credentialVerificationRepository,
                             EmailService emailService,
                             PasswordEncoder passwordEncoder) {
        this.credentialRepository = credentialRepository;
        this.userSyncProducer = userSyncProducer;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.passwordResetService = passwordResetService;
        this.credentialVerificationRepository = credentialVerificationRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public List<CredentialRespDTO> findCredentials() {
        return credentialRepository.findAll().stream()
                .map(CredentialBuilder::toCredentialRespDTO)
                .collect(Collectors.toList());
    }

    public CredentialRespDTO findCredentialById(UUID id) {
        Credential credential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(id.toString()));
        return CredentialBuilder.toCredentialRespDTO(credential);
    }

    public CredentialRespDTO findCredentialByEmail(String email) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));
        return CredentialBuilder.toCredentialRespDTO(credential);
    }

    // Transactional because we send a message through RabbitMQ to another microservice, so we need to make sure it remains consistent
    @Transactional
    public UUID register(RegisterReqDTO dto) {
        // 1. Verificăm dacă email-ul există deja
        Optional<Credential> existingUser = credentialRepository.findByEmail(dto.getEmail());
        if (existingUser.isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        // Initially the user is in pending verification state
        Credential credential = new Credential(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                AccountStatus.PENDING_VERIFICATION
        );

        // all new accounts have by default BUYER and SELLER permissions
        credential.addRole("BUYER");
        credential.addRole("SELLER");

        // Inițializăm verificarea și generăm codul
        CredentialVerification verification = new CredentialVerification(credential);
        verification.generateNewCode();
        credential.setVerificationData(verification);

        // Salvăm în baza de date (salvează și în tabela credentials și în user_roles datorită CascadeType.ALL)
        credential = credentialRepository.save(credential);

        // Trimitem mail pt verificare cont
        emailService.sendVerificationEmail(credential.getEmail(), verification.getVerificationCode());

        // Send a message to the User MS to notify it about the new user
        try {
            userSyncProducer.sendUserCreatedEvent(credential.getId(), dto);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync message to User MS. Rolling back.", e);
            // Note: by throwing an exception, the transaction will be rolled back because of the @Transactional annotation
            throw new RuntimeException("Registration failed, please try again");
        }

        return credential.getId();
    }

    public AuthDTO login(@Valid LoginReqDTO loginReqDTO) {
        Credential credential = credentialRepository.findByEmail(loginReqDTO.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException(loginReqDTO.getEmail()));

        if (!passwordEncoder.matches(loginReqDTO.getPassword(), credential.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect password");
        }

        if (!credential.getStatus().name().equals("ACTIVE")) {
            throw new AccountNotActiveException(credential.getStatus().name());
        }

        // We get the list of roles associated with the user
        List<String> roles = credential.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        // We choose the first role associated with the user
        String activeRole = roles.get(0);

        // Generăm Access Token
        String accessToken = jwtService.generateToken(credential.getEmail(), activeRole, roles);

        // Generăm Refresh Token
        String refreshToken = refreshTokenService.createRefreshToken(credential);

        return new AuthDTO(accessToken, refreshToken, credential.getId(), credential.getEmail(), activeRole, roles);
    }

    public void logout(String refreshToken) {
        // Revocăm refresh token-ul pentru a bloca sesiunea la următoarea expirare a JWT-ului
        refreshTokenService.revokeToken(refreshToken);
    }

    public String switchRole(String email, String targetRole) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        List<String> roles = credential.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        // Verificăm dacă utilizatorul deține rolul pe care vrea să-l activeze
        if (!roles.contains(targetRole.toUpperCase())) {
            throw new IllegalArgumentException("User does not have the " + targetRole + " rights");
        }

        // Generăm și returnăm un nou Access Token (JWT) cu noul rol activ
        return jwtService.generateToken(email, targetRole.toUpperCase(), roles);
    }

    @Transactional
    public void addRole(UUID targetUserId, String newRole) {
        Credential credential = credentialRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(targetUserId.toString()));

        String upperRole = newRole.toUpperCase();

        // Verificăm dacă are deja rolul pentru a evita duplicatele
        boolean alreadyHasRole = credential.getRoles().stream()
                .anyMatch(r -> r.getRole().equals(upperRole));

        if (alreadyHasRole) {
            throw new IllegalArgumentException("User already has the role: " + upperRole);
        }

        credential.addRole(upperRole);
        // CascadeType.ALL se ocupă să insereze în tabela user_roles
        credentialRepository.save(credential);
        LOGGER.info("Role {} added for user {}", upperRole, targetUserId);
    }

    // Changes password when the user is logged in
    @Transactional
    public void changePassword(UUID userId, String oldPassword, String newPassword) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId.toString()));

        if (!passwordEncoder.matches(oldPassword, credential.getPasswordHash())) {
            throw new IllegalArgumentException("Incorrect old password");
        }

        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
        LOGGER.info("Password updated successfully for user {}", userId);
    }

    // changes password when the user is logged off
    @Transactional
    public void forgotPassword(String email) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        // Dacă contul e suspendat sau inactiv, nu putem reseta parola
        if (credential.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active. Either the account is suspended or you must activate it");
        }

        // Generăm token-ul de resetare
        String token = passwordResetService.createResetToken(credential);

        // Trimitere link de resetare parola pe mail
        emailService.sendResetPasswordEmail(email, token);
        LOGGER.info("Reset password email sent to: {}", email);
    }

    // The user calls this endpoint when they click on the link in the email
    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetService.findByToken(token);
        if (resetToken == null) {
            throw new IllegalArgumentException("Invalid or non-existent token");
        }

        // Verificăm dacă a fost deja folosit (atac replay)
        if (resetToken.isUsed()) {
            throw new IllegalArgumentException("This token has already been used");
        }

        // Verificăm dacă a expirat
        if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("This token has expired");
        }

        // Totul este ok, reseteaza parola
        Credential credential = resetToken.getCredential();

        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);

        // Seteaza token-ul ca folosit
        passwordResetService.useToken(token);

        LOGGER.info("Password successfully reset for user {}", credential.getId());
    }

    @Transactional
    public void requestEmailUpdate(UUID userId, String newEmail) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId.toString()));

        if (credentialRepository.findByEmail(newEmail).isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        CredentialVerification verification = credential.getVerificationData();
        if (verification == null) {
            verification = new CredentialVerification(credential);
            credential.setVerificationData(verification);
        }

        // Salvăm email-ul dorit temporar și generăm cod
        verification.setPendingEmail(newEmail);
        verification.generateNewCode();

        credentialRepository.save(credential);

        // Trimitem mail pt verificare cont catre adresa noua
        emailService.sendVerificationEmail(newEmail, verification.getVerificationCode());
    }

    @Transactional
    public void confirmEmailUpdate(UUID userId, String code) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId.toString()));

        CredentialVerification verification = credential.getVerificationData();

        if (verification == null || verification.getPendingEmail() == null) {
            throw new IllegalArgumentException("No pending email update found");
        }

        if (verification.getVerificationCode() == null || !verification.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (verification.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired");
        }

        String newEmail = verification.getPendingEmail();

        // Update email
        credential.setEmail(newEmail);
        verification.clearCode();
        verification.setPendingEmail(null);

        credentialRepository.save(credential);
    }


    // Directly called by the admin to change the state of the account (OBS: cannot change the state back to PENDING_VERIFICATION)
    @Transactional
    public void updateStatus(UUID targetUserId, AccountStatus newStatus) {
        if(newStatus == AccountStatus.PENDING_VERIFICATION) return; // dont allow this

        Credential credential = credentialRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(targetUserId.toString()));

        credential.setStatus(newStatus);
        credentialRepository.save(credential);
        LOGGER.info("Status updated to {} for user {}", newStatus, targetUserId);
    }

    @Transactional
    public void activateAccount(String email, String code) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        if (credential.getStatus() == AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is already active");
        }

        CredentialVerification verification = credential.getVerificationData();

        if (verification.getVerificationCode() == null || !verification.getVerificationCode().equals(code)) {
            throw new IllegalArgumentException("Invalid verification code");
        }

        if (verification.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Verification code has expired");
        }

        // Cod corect -> Activăm contul și ștergem codul
        credential.setStatus(AccountStatus.ACTIVE);
        verification.clearCode();

        credentialRepository.save(credential);
        LOGGER.info("Account activated successfully for user {}", credential.getId());
    }

    @Transactional
    public void resendVerificationCode(String email) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        CredentialVerification verification = credential.getVerificationData();

        // Regula: dacă e ACTIVE și NU are un email în așteptare, nu are de ce să ceară cod
        if (credential.getStatus() == AccountStatus.ACTIVE &&
                (verification == null || verification.getPendingEmail() == null)) {
            throw new IllegalArgumentException("Account is already active and there is no pending email update.");
        }

        // Inițializăm datele de verificare dacă cumva lipsesc (edge case protection)
        if (verification == null) {
            verification = new CredentialVerification(credential);
            credential.setVerificationData(verification);
        }

        // Generăm un cod nou (care va rescrie codul vechi expirat)
        verification.generateNewCode();

        credentialRepository.save(credential);

        // Determinam unde trimitem mail-ul: pe adresa nouă (dacă există) sau pe cea curentă
        String targetEmail = (verification.getPendingEmail() != null)
                ? verification.getPendingEmail()
                : credential.getEmail();

        // Trimitem noul cod generat
        emailService.sendVerificationEmail(targetEmail, verification.getVerificationCode());
        LOGGER.info("A new verification code was generated and sent to: {}", targetEmail);
    }

    @Transactional
    public void delete(UUID id) {
        if (!credentialRepository.existsById(id)) {
            throw new ResourceNotFoundException(id.toString());
        }
        credentialRepository.deleteById(id);
        try{
            userSyncProducer.sendUserDeletedEvent(id);
        }catch (Exception e){
            LOGGER.error("Failed to send sync message to User MS. Rolling back.", e);
            throw new RuntimeException("Deletion failed, please try again");
        }
    }
}