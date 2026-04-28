package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.ProductStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class ProductFilter {
    private UUID categoryId;
    private UUID vendorId;
    private String searchTerm;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private BigDecimal minRating;
    private ProductStatus status;
    private int page;
    private int size;
    private String sortBy;
    private String sortDirection;
}
