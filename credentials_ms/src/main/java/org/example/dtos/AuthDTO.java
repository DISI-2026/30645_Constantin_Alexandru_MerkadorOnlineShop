package org.example.dtos;

import java.util.List;
import java.util.UUID;

// this DTO contains all the data the users need after login
public class AuthDTO {
    private String token;

    private String refreshToken;

    private UUID id;

    private String email;

    private String activeRole;

    private List<String> roles;

    public AuthDTO(String token, String refreshToken, UUID id, String email, String activeRole, List<String> roles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.activeRole = activeRole;
        this.roles = roles;
    }

    // Getters & Setters
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }
    public String getRefreshToken() {
        return refreshToken;
    }
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    public UUID getId() {
        return id;
    }
    public void setId(UUID id) {
        this.id = id;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getActiveRole() {
        return activeRole;
    }
    public void setActiveRole(String activeRole) {
        this.activeRole = activeRole;
    }
    public List<String> getRoles() {
        return roles;
    }
    public void setRoles(List<String> roles) {this.roles = roles;}
}
