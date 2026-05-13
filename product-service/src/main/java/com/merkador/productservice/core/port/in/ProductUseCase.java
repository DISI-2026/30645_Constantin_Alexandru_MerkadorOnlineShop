package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.Product;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.UUID;

public interface ProductUseCase {

    Product createProduct(Product product);

    Product updateProduct(UUID id, Product updated, UUID sellerId);

    void deleteProduct(UUID id, UUID sellerId);

    Product getProductById(UUID id);

    Page<Product> searchProducts(ProductFilter filter);

    Product updateStock(UUID id, int quantity, UUID sellerId);

    Product updatePrice(UUID id, BigDecimal newPrice, UUID sellerId);

    Product activateProduct(UUID id, UUID sellerId);

    Product deactivateProduct(UUID id, UUID sellerId);

    void reserveStock(UUID productId, int quantity);

    void releaseStock(UUID productId, int quantity);

    void updateRating(UUID productId, BigDecimal newAvg, int reviewCount);
    
    void deleteAllProductsBySellerId(UUID sellerId);
}
