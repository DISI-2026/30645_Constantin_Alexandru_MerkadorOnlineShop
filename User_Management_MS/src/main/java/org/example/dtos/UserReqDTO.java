package org.example.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;


// This DTO comes from the client to the server, and it's supposed to reflect the fields
// that are either manipulated in the frontend or need to be passed from the frontend when making an update to a record
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserReqDTO {

    @NotBlank(message = "full name is required")
    private String fullName;

    private String address;

    @NotBlank(message = "email is required")
    @Email
    private String email;

    public UserReqDTO(){}

    public UserReqDTO(String fullName, String address, String email){
        this.fullName = fullName;
        this.address = address;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserReqDTO that = (UserReqDTO) o;
        return Objects.equals(fullName, that.fullName) && Objects.equals(address, that.address) && Objects.equals(email, that.email);
    }
    @Override public int hashCode() { return Objects.hash(fullName, address, email); }
}
