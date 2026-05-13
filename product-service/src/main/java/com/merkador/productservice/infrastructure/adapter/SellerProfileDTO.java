package com.merkador.productservice.infrastructure.adapter;

import java.util.Set;

public class SellerProfileDTO {
    private boolean verified;
    private Set<String> authorizedCategories;

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public Set<String> getAuthorizedCategories() {
        return authorizedCategories;
    }

    public void setAuthorizedCategories(Set<String> authorizedCategories) {
        this.authorizedCategories = authorizedCategories;
    }
}
