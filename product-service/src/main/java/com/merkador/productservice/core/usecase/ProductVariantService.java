package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.ProductVariant;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.in.ProductVariantUseCase;
import com.merkador.productservice.core.port.out.ProductRepository;
import com.merkador.productservice.core.port.out.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductVariantService implements ProductVariantUseCase {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductVariant addVariant(UUID productId, ProductVariant variant, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        if (variantRepository.existsBySku(variant.getSku())) {
            throw new BusinessException("SKU already exists: " + variant.getSku());
        }
        variant.setProductId(productId);
        return variantRepository.save(variant);
    }

    @Override
    @Transactional
    public ProductVariant updateVariant(UUID productId, UUID variantId, ProductVariant updated, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        ProductVariant existing = variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));

        if (!existing.getSku().equals(updated.getSku()) && variantRepository.existsBySku(updated.getSku())) {
            throw new BusinessException("SKU already exists: " + updated.getSku());
        }

        existing.setSku(updated.getSku());
        existing.setLabel(updated.getLabel());
        existing.setPriceModifier(updated.getPriceModifier());
        existing.setStock(updated.getStock());
        return variantRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteVariant(UUID productId, UUID variantId, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        variantRepository.findById(variantId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductVariant", variantId));
        variantRepository.deleteById(variantId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductVariant> getVariantsForProduct(UUID productId) {
        return variantRepository.findByProductId(productId);
    }

    private void assertProductOwnership(UUID productId, UUID vendorId) {
        if (!productRepository.existsByIdAndVendorId(productId, vendorId)) {
            throw new BusinessException("Product not found or does not belong to vendor.");
        }
    }
}
