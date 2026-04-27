package com.merkador.productservice.presentation.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
public class CreateProductRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 500)
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(max = 500)
    private String slug;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotBlank(message = "Currency is required")
    @Size(min = 3, max = 3)
    private String currency;

    @Min(value = 0, message = "Stock must be non-negative")
    private int stock;

    @NotNull(message = "Category ID is required")
    private UUID categoryId;

    @Valid
    private List<ProductImageRequest> images = new ArrayList<>();

    @Valid
    private List<ProductVariantRequest> variants = new ArrayList<>();
}
