package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.Product;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductUseCase {

    Product createProduct(Product product);

    Product updateProduct(UUID id, Product updated, UUID vendorId);

    void deleteProduct(UUID id, UUID vendorId);

    Product getProductById(UUID id);

    Page<Product> searchProducts(ProductFilter filter);

    Product updateStock(UUID id, int quantity, UUID vendorId);

    Product updatePrice(UUID id, BigDecimal newPrice, UUID vendorId);

    Product activateProduct(UUID id, UUID vendorId);

    Product deactivateProduct(UUID id, UUID vendorId);

    /**
     * Called by Order Service via RabbitMQ: reserve stock atomically.
     */
    void reserveStock(UUID productId, int quantity);

    /**
     * Called by Order Service via RabbitMQ: release reserved stock on cancellation.
     */
    void releaseStock(UUID productId, int quantity);

    /**
     * Called by Review Service via RabbitMQ: update denormalized rating.
     */
    void updateRating(UUID productId, BigDecimal newAvg, int reviewCount);
}
