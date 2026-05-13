package com.merkador.productservice.presentation.dto.response;

import com.merkador.productservice.core.domain.ProductStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ProductResponse {
    private UUID id;
    private UUID sellerId;
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
    private List<ProductImageResponse> images;
    private boolean verified;
}
