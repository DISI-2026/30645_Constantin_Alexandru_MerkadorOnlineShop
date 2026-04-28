package org.example.postservice.service;

import org.example.postservice.dto.ReviewRequestDto;
import org.example.postservice.dto.ReviewResponseDto;
import org.example.postservice.dto.VendorReplyRequestDto;

import java.util.List;
import java.util.UUID;

public interface ReviewService {
    ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto);
    ReviewResponseDto addVendorReply(UUID reviewId, VendorReplyRequestDto vendorReplyRequestDto);
    List<ReviewResponseDto> getReviewsByProductId(String productId);
}
