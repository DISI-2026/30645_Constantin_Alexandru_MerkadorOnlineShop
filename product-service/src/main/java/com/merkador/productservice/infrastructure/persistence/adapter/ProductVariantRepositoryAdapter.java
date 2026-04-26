package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.ProductVariant;
import com.merkador.productservice.core.port.out.ProductVariantRepository;
import com.merkador.productservice.infrastructure.persistence.entity.ProductVariantEntity;
import com.merkador.productservice.infrastructure.persistence.repository.JpaProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProductVariantRepositoryAdapter implements ProductVariantRepository {

    private final JpaProductVariantRepository jpa;
    private final PersistenceMapper mapper;

    @Override
    public ProductVariant save(ProductVariant variant) {
        return mapper.toDomain(jpa.save(mapper.toEntity(variant)));
    }

    @Override
    public Optional<ProductVariant> findById(UUID id) {
        return jpa.findById(id).map(mapper::toDomain);
    }

    @Override
    public List<ProductVariant> findByProductId(UUID productId) {
        return jpa.findByProductId(productId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpa.existsBySku(sku);
    }

    @Override
    public void deleteById(UUID id) {
        jpa.deleteById(id);
    }
}
