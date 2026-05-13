package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.in.ProductFilter;
import com.merkador.productservice.core.port.out.ProductRepository;
import com.merkador.productservice.infrastructure.persistence.entity.ProductEntity;
import com.merkador.productservice.infrastructure.persistence.repository.JpaProductRepository;
import com.merkador.productservice.infrastructure.persistence.repository.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private final JpaProductRepository jpa;
    private final PersistenceMapper mapper;

    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            return mapper.toDomain(jpa.save(mapper.toEntity(product)));
        }

        ProductEntity existingEntity = jpa.findById(product.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Product", product.getId()));

        existingEntity.setSellerId(product.getSellerId());
        existingEntity.setCategoryId(product.getCategoryId());
        existingEntity.setTitle(product.getTitle());
        existingEntity.setSlug(product.getSlug());
        existingEntity.setDescription(product.getDescription());
        existingEntity.setPrice(product.getPrice());
        existingEntity.setCurrency(product.getCurrency());
        existingEntity.setStock(product.getStock());
        existingEntity.setStatus(product.getStatus());
        existingEntity.setAvgRating(product.getAvgRating());
        existingEntity.setReviewCount(product.getReviewCount());

        return mapper.toDomain(jpa.save(existingEntity));
    }

    @Override
    public Optional<Product> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return jpa.findBySlug(slug).map(mapper::toDomain);
    }

    @Override
    public Page<Product> findAll(ProductFilter filter) {
        Sort sort = buildSort(filter);
        Pageable pageable = PageRequest.of(filter.getPage(), filter.getSize(), sort);
        return jpa.findAll(ProductSpecification.of(filter), pageable)
                .map(mapper::toDomain);
    }

    @Override
    public boolean existsBySlug(String slug) {
        return jpa.existsBySlug(slug);
    }

    @Override
    public boolean existsByIdAndSellerId(UUID id, UUID sellerId) {
        return jpa.existsByIdAndSellerId(id, sellerId);
    }

    @Override
    public void deleteById(UUID id) {
        ProductEntity entity = jpa.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        jpa.delete(entity);
    }
    
    @Override
    public Double calculateAverageRatingForSeller(UUID sellerId) {
        return jpa.calculateAverageRatingForSeller(sellerId);
    }

    @Override
    public void deleteAllBySellerId(UUID sellerId) {
        jpa.deleteAllBySellerId(sellerId);
    }

    private Sort buildSort(ProductFilter filter) {
        String sortBy = filter.getSortBy() != null ? filter.getSortBy() : "createdAt";
        Sort.Direction direction = "asc".equalsIgnoreCase(filter.getSortDirection())
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        return Sort.by(direction, sortBy);
    }
}
