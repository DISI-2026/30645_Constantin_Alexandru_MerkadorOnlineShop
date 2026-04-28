package org.example.postservice.infrastructure.repository;

import org.example.postservice.infrastructure.entity.ReviewEntity;
import org.example.postservice.infrastructure.entity.VendorReplyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorReplyRepository extends JpaRepository<VendorReplyEntity, UUID> {
    Optional<VendorReplyEntity> findByReview(ReviewEntity review);
}
