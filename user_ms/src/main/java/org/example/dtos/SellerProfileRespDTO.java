package org.example.dtos;

import java.util.UUID;

public class SellerProfileRespDTO {

    private UUID userId;

    private String shopName;

    private String shopSlug;

    private String description;

    private Double avgRating = 0.0;

    private Integer totalSales = 0;

    private Boolean verified;

    public SellerProfileRespDTO() {}

    public SellerProfileRespDTO(UUID userId, String shopName, String shopSlug, String description, Double avgRating, Integer totalSales, Boolean verified) {
        this.userId = userId;
        this.shopName = shopName;
        this.shopSlug = shopSlug;
        this.description = description;
        this.avgRating = avgRating;
        this.totalSales = totalSales;
        this.verified = verified;
    }

    public UUID getUserId() { return userId; }
    public String getShopName() { return shopName; }
    public String getShopSlug() { return shopSlug; }
    public String getDescription() { return description; }
    public Double getAvgRating() { return avgRating; }
    public Integer getTotalSales() { return totalSales; }
    public Boolean getVerified() { return verified; }

    public void setUserId(UUID userId) { this.userId = userId; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
    public void setDescription(String description) { this.description = description; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public void setTotalSales(Integer totalSales) { this.totalSales = totalSales; }
    public void setVerified(Boolean verified) { this.verified = verified; }
}
