package com.merkador.productservice.core.domain;

import com.merkador.productservice.core.exception.InsufficientStockException;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private UUID id;
    private UUID vendorId;
    private UUID categoryId;
    private String title;
    private String slug;
    private String description;
    private BigDecimal price;
    private String currency;
    private int stock;
    private ProductStatus status;
    private BigDecimal avgRating;
    private int reviewCount;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;

    @Builder.Default
    private List<ProductImage> images = new ArrayList<>();

    @Builder.Default
    private List<ProductVariant> variants = new ArrayList<>();

    // -------------------------------------------------------
    // Domain behaviour
    // -------------------------------------------------------

    public boolean isAvailable() {
        return ProductStatus.ACTIVE.equals(status) && stock > 0;
    }

    public void activate() {
        this.status = ProductStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = ProductStatus.INACTIVE;
    }

    public void markDeleted() {
        this.status = ProductStatus.DELETED;
    }

    public void reserveStock(int quantity) {
        if (stock < quantity) {
            throw new InsufficientStockException(id, quantity, stock);
        }
        this.stock -= quantity;
    }

    public void releaseStock(int quantity) {
        this.stock += quantity;
    }

    public void updateRating(BigDecimal newAvg, int newCount) {
        this.avgRating = newAvg;
        this.reviewCount = newCount;
    }
}
