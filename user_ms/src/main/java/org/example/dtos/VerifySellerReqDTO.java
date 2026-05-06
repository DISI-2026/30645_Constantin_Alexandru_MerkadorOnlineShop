package org.example.dtos;

import jakarta.validation.constraints.NotNull;
import java.util.Set;

public class VerifySellerReqDTO {

    @NotNull(message = "Authorized categories list cannot be null")
    private Set<String> authorizedCategories;

    public Set<String> getAuthorizedCategories() {
        return authorizedCategories;
    }

    public void setAuthorizedCategories(Set<String> authorizedCategories) {
        this.authorizedCategories = authorizedCategories;
    }
}