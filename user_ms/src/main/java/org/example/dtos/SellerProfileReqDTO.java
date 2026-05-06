package org.example.dtos;

import jakarta.validation.constraints.NotBlank;

public class SellerProfileReqDTO {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Description is required")
    private String description;

    private String shopSlug;

    public SellerProfileReqDTO() {}

    public SellerProfileReqDTO(String shopName, String description, String shopSlug) {
        this.shopName = shopName;
        this.description = description;
        this.shopSlug = shopSlug;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getShopSlug() {
        return shopSlug;
    }

    public void setShopSlug(String shopSlug) {
        this.shopSlug = shopSlug;
    }
}