package org.example.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "seller_profiles")
public class SellerProfile {

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private UserProfile userProfile;

    @NotBlank(message = "Shop name is required")
    @Column(name = "shop_name", nullable = false)
    private String shopName;

    @NotBlank(message = "Shop slug is required")
    @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must be lowercase, alphanumeric, hyphen-separated")
    @Size(min = 3, max = 64, message = "Slug must be between 3 and 64 characters")
    @Column(name = "shop_slug", nullable = false, unique = true)
    private String shopSlug;

    @Column(name = "description", length = 1000)
    private String description;

    @Column(name = "avg_rating")
    private Double avgRating = 0.0;

    @Column(name = "total_sales")
    private Integer totalSales = 0;

    @Column(name = "is_verified")
    private Boolean verified = false;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ElementCollection
    @CollectionTable(name = "authorized_categories", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "category_slug")
    private Set<String> authorizedCategories = new HashSet<>();

    public SellerProfile() {}

    // Getters & Setters
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UserProfile getUserProfile() { return userProfile; }
    public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
    public String getShopName() { return shopName; }
    public void setShopName(String shopName) { this.shopName = shopName; }
    public String getShopSlug() { return shopSlug; }
    public void setShopSlug(String shopSlug) { this.shopSlug = shopSlug; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getAvgRating() { return avgRating; }
    public void setAvgRating(Double avgRating) { this.avgRating = avgRating; }
    public Integer getTotalSales() { return totalSales; }
    public void setTotalSales(Integer totalSales) { this.totalSales = totalSales; }
    public Boolean getVerified() { return verified; }
    public void setVerified(Boolean verified) { this.verified = verified; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Set<String> getAuthorizedCategories() { return authorizedCategories; }
    public void setAuthorizedCategories(Set<String> authorizedCategories) { this.authorizedCategories = authorizedCategories; }
}