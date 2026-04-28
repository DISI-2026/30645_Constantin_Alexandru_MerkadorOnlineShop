package org.example.postservice.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class VendorReplyResponseDto {
    private UUID id;
    private UUID vendorId;
    private String body;
    private LocalDateTime createdAt;
}
