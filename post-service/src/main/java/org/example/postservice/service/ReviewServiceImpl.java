package org.example.postservice.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.example.postservice.dto.ReviewRequestDto;
import org.example.postservice.dto.ReviewResponseDto;
import org.example.postservice.dto.VendorReplyRequestDto;
import org.example.postservice.infrastructure.entity.ProductRatingAggregateEntity;
import org.example.postservice.infrastructure.entity.ReviewEntity;
import org.example.postservice.infrastructure.entity.VendorReplyEntity;
import org.example.postservice.infrastructure.repository.ProductRatingAggregateRepository;
import org.example.postservice.infrastructure.repository.ReviewRepository;
import org.example.postservice.infrastructure.repository.VendorReplyRepository;
import org.example.postservice.mapper.ReviewMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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

        updateProductRatingAggregate(savedReview);

        ReviewResponseDto responseDto = ReviewMapper.toDto(savedReview);
        notificationPublisher.publishReviewNotification(responseDto);

        return responseDto;
    }

    @Override
    @Transactional
    public ReviewResponseDto addVendorReply(UUID reviewId, VendorReplyRequestDto vendorReplyRequestDto) {
        // Extrage UUID-ul vendorului autentificat din JWT
        String authenticatedVendorId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        
        // Verifica daca vendors-ul din request este egal cu cel autentificat
        if (!authenticatedVendorId.equals(vendorReplyRequestDto.getVendorId().toString())) {
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

        return responseDto;
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

    private void updateProductRatingAggregate(ReviewEntity review) {
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

        switch (review.getRating()) {
            case 1 -> aggregate.setCount1Star(aggregate.getCount1Star() + 1);
            case 2 -> aggregate.setCount2Star(aggregate.getCount2Star() + 1);
            case 3 -> aggregate.setCount3Star(aggregate.getCount3Star() + 1);
            case 4 -> aggregate.setCount4Star(aggregate.getCount4Star() + 1);
            case 5 -> aggregate.setCount5Star(aggregate.getCount5Star() + 1);
        }

        aggregateRepository.save(aggregate);
    }
}
