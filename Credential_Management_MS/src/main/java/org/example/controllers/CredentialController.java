package org.example.controllers;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.example.dtos.CredentialReqDTO;
import org.example.dtos.CredentialRespDTO;
import org.example.dtos.AuthDTO;
import org.example.services.CredentialService;
import org.example.services.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
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

    @GetMapping
    public ResponseEntity<List<CredentialRespDTO>> getAllCredentials() {
        return ResponseEntity.ok().body(credentialService.findCredentials());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredentialRespDTO> getCredentialById(@PathVariable UUID id) {
        return ResponseEntity.ok().body(credentialService.findCredentialById(id));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthDTO> login(@Valid @RequestBody CredentialReqDTO credentialReqDTO) {
        boolean authenticated = credentialService.auth(credentialReqDTO);

        if (!authenticated) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String token = jwtService.generateToken(credentialReqDTO.getUsername());
        CredentialRespDTO credentialRespDTO = credentialService.findCredentialByUsername(credentialReqDTO);
        return ResponseEntity.ok(new AuthDTO(token,credentialRespDTO.getId(), credentialRespDTO.getUsername(), credentialRespDTO.getRole()));
    }

    @PostMapping("/{id}/signup")
    public ResponseEntity<UUID> signup(@PathVariable UUID id, @Valid @RequestBody String rawJson) {
        return ResponseEntity.ok().body(credentialService.register(id, rawJson));
    }

    @PutMapping("/{id}/update")
    public ResponseEntity<?> updateUser(@PathVariable UUID id, //here we send a message if maybe the password isn't correct when trying to change it
                                        @Valid @RequestBody String rawJson) {
        try {
            credentialService.update(id, rawJson);
            return ResponseEntity.noContent().build();
        } catch (BadRequestException e) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteCredential(@PathVariable UUID id) {
        credentialService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
