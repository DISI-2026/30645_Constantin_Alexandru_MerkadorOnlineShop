package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.ProductVariant;

import java.util.List;
import java.util.UUID;

public interface ProductVariantUseCase {

    ProductVariant addVariant(UUID productId, ProductVariant variant, UUID vendorId);

    ProductVariant updateVariant(UUID productId, UUID variantId, ProductVariant updated, UUID vendorId);

    void deleteVariant(UUID productId, UUID variantId, UUID vendorId);

    List<ProductVariant> getVariantsForProduct(UUID productId);
}
