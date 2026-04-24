package org.example.services;

import org.apache.coyote.BadRequestException;
import org.apache.tomcat.util.json.JSONParser;
import org.example.dtos.CredentialReqDTO;
import org.example.dtos.CredentialRespDTO;
import org.example.dtos.builders.CredentialsBuilder;
import org.example.entities.Credential;
import org.example.handlers.exceptions.model.ResourceNotFoundException;
import org.example.repositories.CredentialRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CredentialService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CredentialService.class);
    private final CredentialRepository credentialRepository;
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    @Autowired
    public CredentialService(CredentialRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public List<CredentialRespDTO> findCredentials() {
        List<Credential> credentials = credentialRepository.findAll();
        return credentials.stream()
                .map(CredentialsBuilder::toCredentialRespDTO)
                .collect(Collectors.toList());
    }

    public CredentialRespDTO findCredentialById(UUID id) {
        Optional<Credential> credential = credentialRepository.findById(id);
        if (credential.isPresent()) {
            return CredentialsBuilder.toCredentialRespDTO(credential.get());
        }else  {
            LOGGER.error("Credential not found with id " + id);
            throw new ResourceNotFoundException("Credential not found with id " + id);
        }
    }

    public CredentialRespDTO findCredentialByUsername(CredentialReqDTO credentialReqDTO) {
        Optional<Credential> credential = credentialRepository.findByUsername(credentialReqDTO.getUsername());
        if (credential.isPresent()) {
            return CredentialsBuilder.toCredentialRespDTO(credential.get());
        }else  {
            LOGGER.error("Credential not found with username " + credential.get().getUsername());
            throw new ResourceNotFoundException("Credential not found with username " + credential.get().getUsername());
        }
    }

    public boolean auth(CredentialReqDTO credentialReqDTO) {
        Optional<Credential> credentialOpt = credentialRepository.findByUsername(credentialReqDTO.getUsername());
        if (credentialOpt.isEmpty()) {
            return false;
        }

        return passwordEncoder.matches(credentialReqDTO.getPassword(), credentialOpt.get().getPasswordHash());
    }

    public UUID register(UUID id, String jsonPayload) {
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize with ignored fields
        CredentialReqDTO credentialReqDTO;
        try {
            credentialReqDTO = mapper.readValue(jsonPayload, CredentialReqDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }
        credentialReqDTO.setPassword(passwordEncoder.encode(credentialReqDTO.getPassword()));
        Credential credential = new Credential(id, credentialReqDTO.getUsername(), credentialReqDTO.getPassword(), credentialReqDTO.getRole().toUpperCase());
        credentialRepository.save(credential);

        return credential.getId();
    }

    public void update(UUID id, String jsonPayload) throws BadRequestException {
        ObjectMapper mapper = new ObjectMapper();

        // Deserialize with ignored fields
        CredentialReqDTO credentialReqDTO;
        try {
            credentialReqDTO = mapper.readValue(jsonPayload, CredentialReqDTO.class);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing JSON", e);
        }

        Credential currentCredential = credentialRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Credential not found with id " + id));

        boolean hasNewPassword = credentialReqDTO.getNewPassword() != null
                && !credentialReqDTO.getNewPassword().isEmpty();

        // If new password is being set, verify old password
        if (hasNewPassword) {
            if (!passwordEncoder.matches(credentialReqDTO.getPassword(), currentCredential.getPasswordHash())) {
                throw new BadRequestException("Reset not allowed: incorrect password");
            }
            currentCredential.setPasswordHash(passwordEncoder.encode(credentialReqDTO.getNewPassword()));
        }

        // Always update username and role (unless your business rules say otherwise)
        currentCredential.setUsername(credentialReqDTO.getUsername());
        currentCredential.setRole(credentialReqDTO.getRole().toUpperCase());

        credentialRepository.save(currentCredential);
    }

    public void delete(UUID id) {
        Optional<Credential> credential = credentialRepository.findById(id);
        if(credential.isPresent()) {
            Credential currentCredential = credential.get();
            credentialRepository.deleteById(id);
        }else{
            throw new ResourceNotFoundException("Credential not found with id " + id);
        }
    }
}
