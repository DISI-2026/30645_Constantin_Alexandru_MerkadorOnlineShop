package com.merkador.productservice.infrastructure.persistence.repository;

import com.merkador.productservice.infrastructure.persistence.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {

    Optional<CategoryEntity> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<CategoryEntity> findByParentIdIsNull();

    List<CategoryEntity> findByParentId(UUID parentId);

    @Query("SELECT c FROM CategoryEntity c WHERE c.isActive = true ORDER BY c.sortOrder ASC")
    List<CategoryEntity> findAllActive();

    @Query("SELECT COUNT(c) > 0 FROM CategoryEntity c WHERE c.parentId = :id")
    boolean hasChildren(@Param("id") UUID id);

    @Query("SELECT COUNT(p) > 0 FROM ProductEntity p WHERE p.categoryId = :id")
    boolean hasProducts(@Param("id") UUID id);
}


