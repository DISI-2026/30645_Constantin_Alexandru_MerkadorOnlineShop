package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.in.ProductFilter;
import com.merkador.productservice.core.port.in.ProductUseCase;
import com.merkador.productservice.core.port.out.CategoryRepository;
import com.merkador.productservice.core.port.out.EsOutboxRepository;
import com.merkador.productservice.core.port.out.EventPublisher;
import com.merkador.productservice.core.port.out.ProductRepository;
import com.merkador.productservice.infrastructure.messaging.event.ProductCreatedEvent;
import com.merkador.productservice.infrastructure.messaging.event.ProductDeletedEvent;
import com.merkador.productservice.infrastructure.messaging.event.ProductUpdatedEvent;
import com.merkador.productservice.infrastructure.messaging.event.StockUpdatedEvent;
import com.merkador.productservice.infrastructure.messaging.event.SellerRatingMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService implements ProductUseCase {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final EsOutboxRepository esOutboxRepository;
    private final EventPublisher eventPublisher;

    @Override
    @Transactional
    public Product createProduct(Product product) {
        categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category", product.getCategoryId()));

        if (productRepository.existsBySlug(product.getSlug())) {
            throw new BusinessException("Product slug already exists: " + product.getSlug());
        }

        Product saved = productRepository.save(product);
        esOutboxRepository.enqueue(saved.getId(), "UPSERT");
        eventPublisher.publishProductCreated(new ProductCreatedEvent(saved.getId(), saved.getSellerId(), saved.getTitle()));

        log.info("Created product id={} SELLER={}", saved.getId(), saved.getSellerId());
        return saved;
    }

    @Override
    @Transactional
    public Product updateProduct(UUID id, Product updated, UUID sellerId) {
        Product existing = findOwnedOrThrow(id, sellerId);

        if (!existing.getSlug().equals(updated.getSlug())
                && productRepository.existsBySlug(updated.getSlug())) {
            throw new BusinessException("Product slug already exists: " + updated.getSlug());
        }
        if (!existing.getCategoryId().equals(updated.getCategoryId())) {
            categoryRepository.findById(updated.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", updated.getCategoryId()));
        }

        existing.setTitle(updated.getTitle());
        existing.setSlug(updated.getSlug());
        existing.setDescription(updated.getDescription());
        existing.setPrice(updated.getPrice());
        existing.setCurrency(updated.getCurrency());
        existing.setStock(updated.getStock());
        existing.setCategoryId(updated.getCategoryId());

        Product saved = productRepository.save(existing);
        esOutboxRepository.enqueue(saved.getId(), "UPSERT");
        eventPublisher.publishProductUpdated(new ProductUpdatedEvent(saved.getId(), saved.getSellerId()));

        return saved;
    }

    @Override
    @Transactional
    public void deleteProduct(UUID id, UUID sellerId) {
        Product product = findOwnedOrThrow(id, sellerId);

        productRepository.deleteById(id);

        esOutboxRepository.enqueue(id, "DELETE");
        eventPublisher.publishProductDeleted(new ProductDeletedEvent(id, sellerId));

        log.info("Hard-deleted product id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Product getProductById(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Product> searchProducts(ProductFilter filter) {
        return productRepository.findAll(filter);
    }

    @Override
    @Transactional
    public Product updateStock(UUID id, int quantity, UUID sellerId) {
        Product product = findOwnedOrThrow(id, sellerId);
        product.setStock(quantity);
        Product saved = productRepository.save(product);
        esOutboxRepository.enqueue(id, "UPSERT");
        eventPublisher.publishStockUpdated(new StockUpdatedEvent(id, sellerId, quantity));
        return saved;
    }

    @Override
    @Transactional
    public Product updatePrice(UUID id, BigDecimal newPrice, UUID sellerId) {
        Product product = findOwnedOrThrow(id, sellerId);
        product.setPrice(newPrice);
        Product saved = productRepository.save(product);
        esOutboxRepository.enqueue(id, "UPSERT");
        return saved;
    }

    @Override
    @Transactional
    public Product activateProduct(UUID id, UUID sellerId) {
        Product product = findOwnedOrThrow(id, sellerId);
        product.activate();
        Product saved = productRepository.save(product);
        esOutboxRepository.enqueue(id, "UPSERT");
        return saved;
    }

    @Override
    @Transactional
    public Product deactivateProduct(UUID id, UUID sellerId) {
        Product product = findOwnedOrThrow(id, sellerId);
        product.deactivate();
        Product saved = productRepository.save(product);
        esOutboxRepository.enqueue(id, "UPSERT");
        return saved;
    }

    @Override
    @Transactional
    public void reserveStock(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.reserveStock(quantity);
        productRepository.save(product);
        esOutboxRepository.enqueue(productId, "UPSERT");
        log.info("Reserved {} units for product {}", quantity, productId);
    }

    @Override
    @Transactional
    public void releaseStock(UUID productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.releaseStock(quantity);
        productRepository.save(product);
        esOutboxRepository.enqueue(productId, "UPSERT");
        log.info("Released {} units for product {}", quantity, productId);
    }

    @Override
    @Transactional
    public void updateRating(UUID productId, BigDecimal newAvg, int reviewCount) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));
        product.updateRating(newAvg, reviewCount);
        productRepository.save(product);
        esOutboxRepository.enqueue(productId, "UPSERT");

        Double overallAvg = productRepository.calculateAverageRatingForSeller(product.getSellerId());
        if (overallAvg != null) {
            eventPublisher.publishSellerRatingUpdated(new SellerRatingMessage(product.getSellerId(), overallAvg));
            log.info("Sent rating update for seller {}: {}", product.getSellerId(), overallAvg);
        }
    }

    @Override
    @Transactional
    public void deleteAllProductsBySellerId(UUID sellerId) {
        productRepository.deleteAllBySellerId(sellerId);
        log.info("Deleted all products for seller {}", sellerId);
    }

    // -------------------------------------------------------
    // Helpers
    // -------------------------------------------------------

    private Product findOwnedOrThrow(UUID id, UUID sellerId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));
        if (!product.getSellerId().equals(sellerId)) {
            throw new BusinessException("Product does not belong to this SELLER.");
        }
        return product;
    }
}
