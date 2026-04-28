package org.example.postservice.mapper;

import org.example.postservice.dto.ReviewRequestDto;
import org.example.postservice.dto.ReviewResponseDto;
import org.example.postservice.dto.VendorReplyRequestDto;
import org.example.postservice.dto.VendorReplyResponseDto;
import org.example.postservice.infrastructure.entity.ReviewEntity;
import org.example.postservice.infrastructure.entity.VendorReplyEntity;

public class ReviewMapper {

    public static ReviewEntity toEntity(ReviewRequestDto dto) {
        return ReviewEntity.builder()
                .customerId(dto.getCustomerId())
                .productId(dto.getProductId())
                .orderId(dto.getOrderId())
                .rating(dto.getRating())
                .body(dto.getBody())
                .status("PENDING")
                .build();
    }

    public static ReviewResponseDto toDto(ReviewEntity entity) {
        ReviewResponseDto dto = new ReviewResponseDto();
        dto.setId(entity.getId());
        dto.setCustomerId(entity.getCustomerId());
        dto.setProductId(entity.getProductId());
        dto.setOrderId(entity.getOrderId());
        dto.setRating(entity.getRating());
        dto.setBody(entity.getBody());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }

    public static VendorReplyEntity toEntity(VendorReplyRequestDto dto, ReviewEntity review) {
        return VendorReplyEntity.builder()
                .vendorId(dto.getVendorId())
                .body(dto.getBody())
                .review(review)
                .build();
    }

    public static VendorReplyResponseDto toDto(VendorReplyEntity entity) {
        VendorReplyResponseDto dto = new VendorReplyResponseDto();
        dto.setId(entity.getId());
        dto.setVendorId(entity.getVendorId());
        dto.setBody(entity.getBody());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
    }
}
