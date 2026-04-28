package org.example.postservice.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class VendorReplyRequestDto {
    private UUID vendorId;
    private String body;
}
