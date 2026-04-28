package com.merkador.productservice.presentation.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductVariantRequest {

    @NotBlank
    @Size(max = 255)
    private String sku;

    @NotBlank
    @Size(max = 255)
    private String label;

    @NotNull
    private BigDecimal priceModifier;

    @Min(0)
    private int stock;
}
