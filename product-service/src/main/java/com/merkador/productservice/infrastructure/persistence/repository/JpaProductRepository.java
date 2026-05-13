package com.merkador.productservice.infrastructure.persistence.repository;

import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID>,
        JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByIdAndSellerId(UUID id, UUID sellerId);
    
    @Query("SELECT AVG(p.avgRating) FROM ProductEntity p WHERE p.sellerId = :sellerId AND p.reviewCount > 0")
    Double calculateAverageRatingForSeller(@Param("sellerId") UUID sellerId);
    
    @Modifying
    @Query("DELETE FROM ProductEntity p WHERE p.sellerId = :sellerId")
    void deleteAllBySellerId(@Param("sellerId") UUID sellerId);
}
