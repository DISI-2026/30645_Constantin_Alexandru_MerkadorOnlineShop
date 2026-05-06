package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Set;


@JsonIgnoreProperties(ignoreUnknown = true)
public class UserProfileReqDTO {
    @NotBlank(message = "First name required")
    private String firstName;

    @NotBlank(message = "Last name required")
    private String lastName;

    private String avatarUrl;

    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Invalid phone number")
    @Size(max = 16, message = "Phone number too long")
    private String phone;

    Set<String> preferredCategories;

    public UserProfileReqDTO(){}

    public UserProfileReqDTO(String phone, String avatarUrl, String lastName, String firstName, Set<String> preferredCategories) {
        this.phone = phone;
        this.avatarUrl = avatarUrl;
        this.lastName = lastName;
        this.firstName = firstName;
        this.preferredCategories = preferredCategories;
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

    public Set<String> getPreferredCategories() {
        return preferredCategories;
    }

    public void setPreferredCategories(Set<String> preferredCategories) {
        this.preferredCategories = preferredCategories;
    }
}
