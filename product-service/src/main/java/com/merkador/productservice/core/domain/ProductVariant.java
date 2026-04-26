package com.merkador.productservice.core.domain;

import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariant {

    private UUID id;
    private UUID productId;
    private String sku;
    private String label;
    private BigDecimal priceModifier;
    private int stock;

    public BigDecimal effectivePrice(BigDecimal basePrice) {
        return basePrice.add(priceModifier);
    }
}
