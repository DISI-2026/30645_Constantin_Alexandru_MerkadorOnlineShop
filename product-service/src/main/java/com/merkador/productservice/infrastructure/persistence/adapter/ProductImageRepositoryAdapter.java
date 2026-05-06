package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.port.out.ProductImageRepository;
import com.merkador.productservice.infrastructure.persistence.repository.JpaProductImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductImageRepositoryAdapter implements ProductImageRepository {

    private final JpaProductImageRepository jpa;
    private final PersistenceMapper mapper;

    @Override
    public ProductImage save(ProductImage image) {
        return mapper.toDomain(jpa.save(mapper.toEntity(image)));
    }

    @Override
    public Optional<ProductImage> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProductImage> findByProductId(UUID productId) {
        return jpa.findByProductIdOrderBySortOrderAsc(productId)
                .stream().map(mapper::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }

    @Override
    public void saveAll(List<ProductImage> images) {
        jpa.saveAll(images.stream().map(mapper::toEntity).toList());
    }
}


