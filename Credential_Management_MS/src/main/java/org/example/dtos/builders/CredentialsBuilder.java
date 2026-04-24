package org.example.dtos.builders;

import org.example.dtos.CredentialRespDTO;
import org.example.entities.Credential;

public class CredentialsBuilder {

    public static CredentialRespDTO toCredentialRespDTO(Credential credential) {
        CredentialRespDTO credentialRespDTO = new CredentialRespDTO(credential.getId(), credential.getUsername(), credential.getRole());
        return credentialRespDTO;
    }
}
