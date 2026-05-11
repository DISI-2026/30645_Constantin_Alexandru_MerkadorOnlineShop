package org.example.dtos;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SellerProfileRespDTO {

    private UUID userId;

    private String shopName;

    private String shopSlug;

    private String description;

    private String logoUrl;

    private Double avgRating = 0.0;

    private Double totalSales = 0.0;

    private Boolean verified;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Set<String> authorizedCategories;

    public SellerProfileRespDTO() {}

    public SellerProfileRespDTO(UUID userId, String shopName, String shopSlug, String description, String logoUrl, Double avgRating, Double totalSales, Boolean verified, LocalDateTime createdAt, LocalDateTime updatedAt, Set<String> authorizedCategories) {
        this.userId = userId;
        this.shopName = shopName;
        this.shopSlug = shopSlug;
        this.description = description;
        this.logoUrl = logoUrl;
        this.avgRating = avgRating;
        this.totalSales = totalSales;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorizedCategories = authorizedCategories;
    }

    public UUID getUserId() { return userId; }
    public String getShopName() { return shopName; }
    public String getShopSlug() { return shopSlug; }
    public String getDescription() { return description; }
    public String getLogoUrl() { return logoUrl; }
    public Double getAvgRating() { return avgRating; }
    public Double getTotalSales() { return totalSales; }
    public Boolean getVerified() { return verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Set<String> getAuthorizedCategories() { return authorizedCategories; }

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
    public void setDescription(String description) { this.description = description; }
    public void setLogoUrl(String logoUrl) { this.logoUrl = logoUrl; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public void setTotalSales(Double totalSales) { this.totalSales = totalSales; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setAuthorizedCategories(Set<String> authorizedCategories) { this.authorizedCategories = authorizedCategories; }
}
