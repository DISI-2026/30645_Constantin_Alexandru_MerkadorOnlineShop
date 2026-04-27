package com.merkador.productservice.core.port.in;

import com.merkador.productservice.core.domain.Category;

import java.util.List;
import java.util.UUID;

public interface CategoryUseCase {

    Category createCategory(Category category);

    Category updateCategory(UUID id, Category updated);

    void deleteCategory(UUID id);

    Category getCategoryById(UUID id);

    List<Category> getRootCategories();

    List<Category> getChildCategories(UUID parentId);

    List<Category> getAllActiveCategories();
}
