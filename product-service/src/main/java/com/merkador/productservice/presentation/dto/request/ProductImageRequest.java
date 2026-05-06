package com.merkador.productservice.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductImageRequest {
    @NotBlank
    @Size(max = 1000)
    private String url;

    @Size(max = 500)
    private String altText;

    private int sortOrder;
}


