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

    @Transactional
    public UUID register(RegisterReqDTO dto) {
        credentialRepository.findByEmail(dto.getEmail()).ifPresent(c -> {
            throw new IllegalArgumentException("Email already in use");
        });

        Credential credential = new Credential(
                dto.getEmail(),
                passwordEncoder.encode(dto.getPassword()),
                AccountStatus.PENDING_VERIFICATION
        );
        credential.addRole("BUYER");
        credential.addRole("SELLER");

        CredentialVerification verification = new CredentialVerification(credential);
        verification.generateNewCode();
        credential.setVerificationData(verification);

        credential = credentialRepository.save(credential);
        emailService.sendVerificationEmail(credential.getEmail(), verification.getVerificationCode());

        try {
            userSyncProducer.sendUserCreatedEvent(credential.getId(), dto);
        } catch (Exception e) {
            LOGGER.error("Failed to send sync message to User MS. Rolling back.", e);
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

        if (credential.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(credential.getStatus().name());
        }

        List<String> roles = credential.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        String activeRole = roles.isEmpty() ? null : roles.get(0);

        // CORECTAT: Pasăm și UUID-ul la generarea token-ului
        String accessToken = jwtService.generateToken(credential.getEmail(), credential.getId(), activeRole, roles);
        String refreshToken = refreshTokenService.createRefreshToken(credential);

        return new AuthDTO(accessToken, refreshToken, credential.getId(), credential.getEmail(), activeRole, roles);
    }

    @Transactional
    public AuthDTO refreshToken(String refreshTokenStr) {
        RefreshToken tokenEntity = refreshTokenService.validateAndGetToken(refreshTokenStr);
        Credential user = tokenEntity.getCredential();

        // Vf daca utilizatorul este activ
        if (user.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(user.getStatus().name());
        }

        List<String> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        String activeRole;
        if (roles.contains("ADMIN")) {
            activeRole = "ADMIN";
        } else {
            activeRole = roles.isEmpty() ? null : roles.get(0);
        }

        // Generăm un nou Access Token dar pastram refresh token-ul vechi
        String newAccessToken = jwtService.generateToken(user.getEmail(), user.getId(), activeRole, roles);

        return new AuthDTO(newAccessToken, refreshTokenStr, user.getId(), user.getEmail(), activeRole, roles);
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeToken(refreshToken);
    }

    public String switchRole(String email, String targetRole) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        List<String> roles = credential.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toList());

        if (!roles.contains(targetRole.toUpperCase())) {
            throw new IllegalArgumentException("User does not have the " + targetRole + " rights");
        }

        return jwtService.generateToken(credential.getEmail(), credential.getId(), targetRole.toUpperCase(), roles);
    }

    @Transactional
    public void addRole(UUID targetUserId, String newRole) {
        Credential credential = credentialRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException(targetUserId.toString()));

        String upperRole = newRole.toUpperCase();

        boolean alreadyHasRole = credential.getRoles().stream()
                .anyMatch(r -> r.getRole().equals(upperRole));

        if (alreadyHasRole) {
            throw new IllegalArgumentException("User already has the role: " + upperRole);
        }

        credential.addRole(upperRole);
        credentialRepository.save(credential);
        LOGGER.info("Role {} added for user {}", upperRole, targetUserId);
    }

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

    @Transactional
    public void forgotPassword(String email) {
        Credential credential = credentialRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(email));

        if (credential.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalArgumentException("Account is not active. Either the account is suspended or you must activate it");
        }

        String token = passwordResetService.createResetToken(credential);
        emailService.sendResetPasswordEmail(email, token);
        LOGGER.info("Reset password email sent to: {}", email);
    }

    @Transactional
    public void resetPasswordWithToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetService.findByToken(token);
        if (resetToken == null || resetToken.isUsed() || resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }

        Credential credential = resetToken.getCredential();
        credential.setPasswordHash(passwordEncoder.encode(newPassword));
        credentialRepository.save(credential);
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

        verification.setPendingEmail(newEmail);
        verification.generateNewCode();
        credentialRepository.save(credential);
        emailService.sendVerificationEmail(newEmail, verification.getVerificationCode());
    }

    @Transactional
    public void confirmEmailUpdate(UUID userId, String code) {
        Credential credential = credentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId.toString()));

        CredentialVerification verification = credential.getVerificationData();
        if (verification == null || verification.getPendingEmail() == null || verification.getVerificationCode() == null || !verification.getVerificationCode().equals(code) || verification.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

        String newEmail = verification.getPendingEmail();
        credential.setEmail(newEmail);
        verification.clearCode();
        verification.setPendingEmail(null);
        credentialRepository.save(credential);
    }

    @Transactional
    public void updateStatus(UUID targetUserId, AccountStatus newStatus) {
        if(newStatus == AccountStatus.PENDING_VERIFICATION) return;

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
        if (verification.getVerificationCode() == null || !verification.getVerificationCode().equals(code) || verification.getCodeExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired verification code");
        }

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
        if (credential.getStatus() == AccountStatus.ACTIVE && (verification == null || verification.getPendingEmail() == null)) {
            throw new IllegalArgumentException("Account is already active and there is no pending email update.");
        }

        if (verification == null) {
            verification = new CredentialVerification(credential);
            credential.setVerificationData(verification);
        }

        verification.generateNewCode();
        credentialRepository.save(credential);

        String targetEmail = (verification.getPendingEmail() != null) ? verification.getPendingEmail() : credential.getEmail();
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
