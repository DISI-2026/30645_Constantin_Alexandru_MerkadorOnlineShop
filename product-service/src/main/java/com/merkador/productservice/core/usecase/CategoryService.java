package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.in.CategoryUseCase;
import com.merkador.productservice.core.port.out.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService implements CategoryUseCase {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional
    public Category createCategory(Category category) {
        if (categoryRepository.existsBySlug(category.getSlug())) {
            throw new BusinessException("Category slug already exists: " + category.getSlug());
        }
        if (category.getParentId() != null) {
            categoryRepository.findById(category.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", category.getParentId()));
        }
        Category saved = categoryRepository.save(category);
        log.info("Created category id={} slug={}", saved.getId(), saved.getSlug());
        return saved;
    }

    @Override
    @Transactional
    public Category updateCategory(UUID id, Category updated) {
        Category existing = findOrThrow(id);

        if (!existing.getSlug().equals(updated.getSlug())
                && categoryRepository.existsBySlug(updated.getSlug())) {
            throw new BusinessException("Category slug already exists: " + updated.getSlug());
        }
        if (updated.getParentId() != null) {
            if (updated.getParentId().equals(id)) {
                throw new BusinessException("Category cannot be its own parent.");
            }
            categoryRepository.findById(updated.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category", updated.getParentId()));
        }

        existing.setName(updated.getName());
        existing.setSlug(updated.getSlug());
        existing.setParentId(updated.getParentId());
        existing.setSortOrder(updated.getSortOrder());
        existing.setActive(updated.isActive());

        return categoryRepository.save(existing);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        findOrThrow(id);
        if (categoryRepository.hasChildren(id)) {
            throw new BusinessException("Cannot delete category with subcategories.");
        }
        if (categoryRepository.hasProducts(id)) {
            throw new BusinessException("Cannot delete category that contains products.");
        }
        categoryRepository.deleteById(id);
        log.info("Deleted category id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public Category getCategoryById(UUID id) {
        return findOrThrow(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getRootCategories() {
        return categoryRepository.findByParentIdIsNull();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getChildCategories(UUID parentId) {
        findOrThrow(parentId);
        return categoryRepository.findByParentId(parentId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findAllActive();
    }

    private Category findOrThrow(UUID id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category", id));
    }
}


