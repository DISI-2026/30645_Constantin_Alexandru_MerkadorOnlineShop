package org.example.orderservice.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderLineDto {
    private String productId;
    private String productTitle;
    private BigDecimal unitPrice;
    private Integer quantity;
}
