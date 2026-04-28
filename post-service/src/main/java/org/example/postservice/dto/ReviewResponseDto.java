package org.example.postservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ReviewResponseDto {
    private UUID id;
    private UUID customerId;
    private String productId;
    private String orderId;
    private Integer rating;
    private String body;
    private String status;
    private LocalDateTime createdAt;
    private VendorReplyResponseDto reply;
}
