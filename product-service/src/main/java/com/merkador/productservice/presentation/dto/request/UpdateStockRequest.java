package com.merkador.productservice.presentation.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class UpdateStockRequest {
    @Min(value = 0, message = "Stock must be non-negative")
    private int stock;
}
