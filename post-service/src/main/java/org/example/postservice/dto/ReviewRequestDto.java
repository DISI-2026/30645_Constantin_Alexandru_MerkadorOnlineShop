package org.example.postservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class ReviewRequestDto {
    private UUID customerId;
    private String productId;
    private String orderId;
    private Integer rating;
    private String body;
}
