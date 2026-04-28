package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.in.ProductImageUseCase;
import com.merkador.productservice.core.port.out.ProductImageRepository;
import com.merkador.productservice.core.port.out.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ProductImageService implements ProductImageUseCase {

    private final ProductImageRepository imageRepository;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public ProductImage addImage(UUID productId, ProductImage image, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        image.setProductId(productId);
        return imageRepository.save(image);
    }

    @Override
    @Transactional
    public void deleteImage(UUID productId, UUID imageId, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        imageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
        imageRepository.deleteById(imageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductImage> getImagesForProduct(UUID productId) {
        return imageRepository.findByProductId(productId);
    }

    @Override
    @Transactional
    public void reorderImages(UUID productId, List<UUID> orderedImageIds, UUID vendorId) {
        assertProductOwnership(productId, vendorId);
        List<ProductImage> images = imageRepository.findByProductId(productId);

        List<ProductImage> reordered = IntStream.range(0, orderedImageIds.size())
                .mapToObj(i -> {
                    UUID imageId = orderedImageIds.get(i);
                    ProductImage img = images.stream()
                            .filter(x -> x.getId().equals(imageId))
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("ProductImage", imageId));
                    img.setSortOrder(i);
                    return img;
                })
                .toList();

        imageRepository.saveAll(reordered);
    }

    private void assertProductOwnership(UUID productId, UUID vendorId) {
        if (!productRepository.existsByIdAndVendorId(productId, vendorId)) {
            throw new BusinessException("Product not found or does not belong to vendor.");
        }
    }
}
