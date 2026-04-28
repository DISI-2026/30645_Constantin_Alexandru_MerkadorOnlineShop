package org.example.postservice.controller;

import lombok.RequiredArgsConstructor;
import org.example.postservice.dto.ReviewRequestDto;
import org.example.postservice.dto.ReviewResponseDto;
import org.example.postservice.dto.VendorReplyRequestDto;
import org.example.postservice.service.ReviewService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponseDto> createReview(@RequestBody ReviewRequestDto reviewRequestDto) {
        ReviewResponseDto createdReview = reviewService.createReview(reviewRequestDto);
        return new ResponseEntity<>(createdReview, HttpStatus.CREATED);
    }

    @PostMapping("/{reviewId}/reply")
    public ResponseEntity<ReviewResponseDto> addVendorReply(@PathVariable UUID reviewId,
                                                            @RequestBody VendorReplyRequestDto vendorReplyRequestDto) {
        ReviewResponseDto updatedReview = reviewService.addVendorReply(reviewId, vendorReplyRequestDto);
        return ResponseEntity.ok(updatedReview);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<List<ReviewResponseDto>> getReviewsByProductId(@PathVariable String productId) {
        List<ReviewResponseDto> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }
}
