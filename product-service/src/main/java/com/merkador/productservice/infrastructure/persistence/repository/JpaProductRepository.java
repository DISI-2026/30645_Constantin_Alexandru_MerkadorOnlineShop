package com.merkador.productservice.infrastructure.persistence.repository;

import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.infrastructure.persistence.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID>,
        JpaSpecificationExecutor<ProductEntity> {

    Optional<ProductEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    boolean existsByIdAndVendorId(UUID id, UUID vendorId);
}
