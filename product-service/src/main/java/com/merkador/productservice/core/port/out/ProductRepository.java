package com.merkador.productservice.core.port.out;

import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.port.in.ProductFilter;
import org.springframework.data.domain.Page;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {

    Product save(Product product);

    Optional<Product> findById(UUID id);

    Optional<Product> findBySlug(String slug);

    Page<Product> findAll(ProductFilter filter);

    boolean existsBySlug(String slug);

    boolean existsByIdAndVendorId(UUID id, UUID vendorId);

    void deleteById(UUID id);
}
