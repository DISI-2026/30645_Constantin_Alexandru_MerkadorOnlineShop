package com.merkador.productservice.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class ProductVariantResponse {
    private UUID id;
    private String sku;
    private String label;
    private BigDecimal priceModifier;
    private int stock;
}
