package org.example.dtos;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

public class UserProfileRespDTO {

    private UUID userId;

    private String firstName;

    private String lastName;

    private String avatarUrl;

    private String phone;

    private LocalDateTime createdDate;

    private LocalDateTime updatedDate;

    private Set<String> preferredCategories;

    public UserProfileRespDTO(){}

    public UserProfileRespDTO(UUID userId, String firstName, String lastName, String avatarUrl, String phone, LocalDateTime createdDate, LocalDateTime updatedDate, Set<String> preferredCategories) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.preferredCategories = preferredCategories;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public Set<String> getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(Set<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }
}
