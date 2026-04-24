package org.example.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

// This DTO goes from the server to the client, and it's supposed to reflect the fields
// that the client needs to display or to later use for identification of records (the id)
public class UserRespDTO {

    private UUID id;

    private String fullName;

    private String address;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private String email;

    public UserRespDTO(){}

    public UserRespDTO(UUID id, String fullName, String address, LocalDateTime createdDate, LocalDateTime updatedDate, String email) {
        this.id = id;
        this.fullName = fullName;
        this.address = address;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.email = email;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
