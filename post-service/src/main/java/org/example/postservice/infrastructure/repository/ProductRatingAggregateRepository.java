package org.example.postservice.infrastructure.repository;

import org.example.postservice.infrastructure.entity.ProductRatingAggregateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRatingAggregateRepository extends JpaRepository<ProductRatingAggregateEntity, String> {
}
