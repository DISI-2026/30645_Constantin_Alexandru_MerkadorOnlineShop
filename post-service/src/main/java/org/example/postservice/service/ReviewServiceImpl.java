package org.example.postservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.postservice.dto.ReviewApprovedMessage;
import org.example.postservice.dto.ReviewRequestDto;
import org.example.postservice.dto.ReviewResponseDto;
import org.example.postservice.dto.VendorReplyRequestDto;
import org.example.postservice.infrastructure.entity.ProductRatingAggregateEntity;
import org.example.postservice.infrastructure.entity.ReviewEntity;
import org.example.postservice.infrastructure.entity.VendorReplyEntity;
import org.example.postservice.infrastructure.repository.ProductRatingAggregateRepository;
import org.example.postservice.infrastructure.repository.ReviewRepository;
import org.example.postservice.infrastructure.repository.VendorReplyRepository;
import org.example.postservice.infrastructure.entity.Rating;
import org.example.postservice.mapper.ReviewMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final VendorReplyRepository vendorReplyRepository;
    private final ProductRatingAggregateRepository aggregateRepository;
    private final ReviewNotificationPublisher notificationPublisher;

    @Override
    @Transactional
    public ReviewResponseDto createReview(ReviewRequestDto reviewRequestDto) {
        ReviewEntity reviewEntity = ReviewMapper.toEntity(reviewRequestDto);
        ReviewEntity savedReview = reviewRepository.save(reviewEntity);

        ProductRatingAggregateEntity updatedAggregate = updateProductRatingAggregate(savedReview);

        ReviewResponseDto responseDto = ReviewMapper.toDto(savedReview);
        notificationPublisher.publishReviewNotification(responseDto);

        ReviewApprovedMessage syncMessage = new ReviewApprovedMessage(
                UUID.fromString(savedReview.getProductId()), 
                updatedAggregate.getAvgRating(), 
                (int) updatedAggregate.getReviewCount()
        );
        notificationPublisher.publishRatingUpdateToProductService(syncMessage);

        return responseDto;
    }

    @Override
    @Transactional
    public ReviewResponseDto addVendorReply(UUID reviewId, VendorReplyRequestDto vendorReplyRequestDto) {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String authenticatedVendorId = principal != null ? principal.toString() : null;
            
            log.info("Attempting to add reply. Auth ID: {}, DTO Vendor ID: {}", authenticatedVendorId, vendorReplyRequestDto.getVendorId());

            if (authenticatedVendorId == null || vendorReplyRequestDto.getVendorId() == null || 
                !authenticatedVendorId.equals(vendorReplyRequestDto.getVendorId().toString())) {
                throw new SecurityException("Vendor can only reply with their own vendor ID");
            }

            ReviewEntity reviewEntity = reviewRepository.findById(reviewId)
                    .orElseThrow(() -> new EntityNotFoundException("Review not found with id: " + reviewId));

            VendorReplyEntity replyEntity = ReviewMapper.toEntity(vendorReplyRequestDto, reviewEntity);
            vendorReplyRepository.save(replyEntity);

            reviewEntity.setStatus("REPLIED");
            ReviewEntity updatedReview = reviewRepository.save(reviewEntity);

            ReviewResponseDto responseDto = ReviewMapper.toDto(updatedReview);
            responseDto.setReply(ReviewMapper.toDto(replyEntity));

            notificationPublisher.publishReviewNotification(responseDto);

            return responseDto;
        } catch (Exception e) {
            log.error("Error adding vendor reply for review {}: {}", reviewId, e.getMessage(), e);
            throw new RuntimeException("Failed to add vendor reply: " + e.getMessage(), e);
        }
    }

    @Override
    public List<ReviewResponseDto> getReviewsByProductId(String productId) {
        return reviewRepository.findByProductId(productId).stream()
                .map(reviewEntity -> {
                    ReviewResponseDto dto = ReviewMapper.toDto(reviewEntity);
                    vendorReplyRepository.findByReview(reviewEntity).ifPresent(reply -> dto.setReply(ReviewMapper.toDto(reply)));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private ProductRatingAggregateEntity updateProductRatingAggregate(ReviewEntity review) {
        ProductRatingAggregateEntity aggregate = aggregateRepository.findById(review.getProductId())
                .orElse(ProductRatingAggregateEntity.builder()
                        .productId(review.getProductId())
                        .avgRating(BigDecimal.ZERO)
                        .reviewCount(0L)
                        .count1Star(0L).count2Star(0L).count3Star(0L).count4Star(0L).count5Star(0L)
                        .build());

        long newReviewCount = aggregate.getReviewCount() + 1;
        BigDecimal totalRating = aggregate.getAvgRating().multiply(BigDecimal.valueOf(aggregate.getReviewCount()));
        BigDecimal newAvgRating = totalRating.add(BigDecimal.valueOf(review.getRating()))
                .divide(BigDecimal.valueOf(newReviewCount), 2, RoundingMode.HALF_UP);

        aggregate.setReviewCount(newReviewCount);
        aggregate.setAvgRating(newAvgRating);

        Rating rating = Rating.fromValue(review.getRating());
        switch (rating) {
            case POOR -> aggregate.setCount1Star(aggregate.getCount1Star() + 1);
            case FAIR -> aggregate.setCount2Star(aggregate.getCount2Star() + 1);
            case GOOD -> aggregate.setCount3Star(aggregate.getCount3Star() + 1);
            case VERY_GOOD -> aggregate.setCount4Star(aggregate.getCount4Star() + 1);
            case EXCELLENT -> aggregate.setCount5Star(aggregate.getCount5Star() + 1);
        }

        return aggregateRepository.save(aggregate);
    }
}
