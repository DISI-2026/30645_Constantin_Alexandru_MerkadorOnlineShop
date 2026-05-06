package com.merkador.productservice.core.port.out;

import com.merkador.productservice.core.domain.ProductImage;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductImageRepository {

    ProductImage save(ProductImage image);

    Optional<ProductImage> findById(UUID id);

    List<ProductImage> findByProductId(UUID productId);

    void deleteById(UUID id);

    void saveAll(List<ProductImage> images);
}


