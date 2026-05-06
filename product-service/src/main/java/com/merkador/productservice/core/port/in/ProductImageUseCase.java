package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.ProductImage;

import java.util.List;
import java.util.UUID;

public interface ProductImageUseCase {

    ProductImage addImage(UUID productId, ProductImage image, UUID sellerId);

    void deleteImage(UUID productId, UUID imageId, UUID sellerId);

    List<ProductImage> getImagesForProduct(UUID productId);

    void reorderImages(UUID productId, List<UUID> orderedImageIds, UUID sellerId);
}


