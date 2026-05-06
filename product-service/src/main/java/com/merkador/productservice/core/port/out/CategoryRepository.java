package com.merkador.productservice.core.port.out;

import com.merkador.productservice.core.domain.Category;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {

    Category save(Category category);

    Optional<Category> findById(UUID id);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdIsNull();

    List<Category> findByParentId(UUID parentId);

    List<Category> findAllActive();

    boolean existsBySlug(String slug);

    void deleteById(UUID id);

    boolean hasChildren(UUID id);

    boolean hasProducts(UUID id);
}


