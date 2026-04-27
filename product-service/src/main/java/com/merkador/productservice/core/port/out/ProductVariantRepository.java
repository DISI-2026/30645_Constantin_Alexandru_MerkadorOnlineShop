package com.merkador.productservice.core.port.out;

import com.merkador.productservice.core.domain.ProductVariant;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductVariantRepository {

    ProductVariant save(ProductVariant variant);

    Optional<ProductVariant> findById(UUID id);

    List<ProductVariant> findByProductId(UUID productId);

    boolean existsBySku(String sku);

    void deleteById(UUID id);
}
