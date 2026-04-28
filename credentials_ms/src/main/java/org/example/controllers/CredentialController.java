package org.example.controllers;

import jakarta.validation.Valid;
import org.example.dtos.LoginReqDTO;
import org.example.dtos.RegisterReqDTO;
import org.example.dtos.CredentialRespDTO;
import org.example.dtos.AuthDTO;
import org.example.entities.AccountStatus;
import org.example.services.CredentialService;
import org.example.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/credentials")
@Validated
public class CredentialController {
    private final CredentialService credentialService;
    private final JwtService jwtService;

    public CredentialController(CredentialService credentialService, JwtService jwtService) {
        this.credentialService = credentialService;
        this.jwtService = jwtService;
    }

    // ==========================================
    // 1. RUTE PUBLICE (fără verificare JWT)
    // ==========================================

    @PostMapping("/add")
    public ResponseEntity<UUID> register(@Valid @RequestBody RegisterReqDTO registerReqDTO) {
        UUID newUserId = credentialService.register(registerReqDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(newUserId);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO> login(@Valid @RequestBody LoginReqDTO loginReqDTO) {
        AuthDTO authDTO = credentialService.login(loginReqDTO);
        return ResponseEntity.ok(authDTO);
    }

    @PostMapping("/activate")
    public ResponseEntity<Void> activateAccount(@RequestParam String email, @RequestParam String code) {
        credentialService.activateAccount(email, code);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-code")
    public ResponseEntity<Void> resendVerificationCode(@RequestParam String email) {
        credentialService.resendVerificationCode(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@RequestParam String email) {
        credentialService.forgotPassword(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Void> resetPasswordWithToken(@RequestParam String token, @RequestBody Map<String, String> body) {
        credentialService.resetPasswordWithToken(token, body.get("newPassword"));
        return ResponseEntity.ok().build();
    }


    // ==========================================
    // 2. RUTE PRIVATE - UTILIZATOR
    // ==========================================

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String refreshToken) {
        credentialService.logout(refreshToken);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/switch-role")
    public ResponseEntity<String> switchRole(@RequestBody Map<String, String> body) {
        String newJwt = credentialService.switchRole(body.get("email"), body.get("targetRole"));
        return ResponseEntity.ok(newJwt);
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        credentialService.changePassword(id, body.get("oldPassword"), body.get("newPassword"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/email-update-request")
    public ResponseEntity<Void> requestEmailUpdate(@PathVariable UUID id, @RequestParam String newEmail) {
        credentialService.requestEmailUpdate(id, newEmail);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/email-update-confirm")
    public ResponseEntity<Void> confirmEmailUpdate(@PathVariable UUID id, @RequestParam String code) {
        credentialService.confirmEmailUpdate(id, code);
        return ResponseEntity.ok().build();
    }


    // ==========================================
    // 3. RUTE PRIVATE - ADMINISTRARE
    // ==========================================

    @GetMapping
    public ResponseEntity<List<CredentialRespDTO>> getAllCredentials() {
        return ResponseEntity.ok().body(credentialService.findCredentials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredentialRespDTO> getCredentialById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(credentialService.findCredentialById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Void> updateStatus(@PathVariable UUID id, @RequestParam AccountStatus newStatus) {
        credentialService.updateStatus(id, newStatus);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/role")
    public ResponseEntity<Void> addRole(@PathVariable UUID id, @RequestParam String newRole) {
        credentialService.addRole(id, newRole);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteCredential(@PathVariable UUID id) {
        credentialService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // 4. RUTA INTERNĂ TRAEFIK FORWARD-AUTH
    // ==========================================

    @GetMapping("/validate")
    public ResponseEntity<Void> validateToken(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        try {
            // CORECTAT: Folosim extractEmail, deoarece subiectul este email-ul
            String email = jwtService.extractEmail(token);
            
            CredentialRespDTO user = credentialService.findCredentialByEmail(email);
            
            if (!Objects.equals(user.getStatus(), "ACTIVE")) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
