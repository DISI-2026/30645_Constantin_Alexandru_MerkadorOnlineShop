package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.out.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CategoryService")
class CategoryServiceTest {

    @Mock CategoryRepository categoryRepository;

    @InjectMocks CategoryService categoryService;

    private Category rootCategory;
    private UUID rootId;

    @BeforeEach
    void setUp() {
        rootId = UUID.randomUUID();
        rootCategory = Category.builder()
                .id(rootId)
                .name("Electronics")
                .slug("electronics")
                .parentId(null)
                .sortOrder(0)
                .isActive(true)
                .build();
    }

    @Nested
    @DisplayName("createCategory")
    class CreateCategory {

        @Test
        @DisplayName("should create root category successfully")
        void shouldCreateRootCategory() {
            when(categoryRepository.existsBySlug("electronics")).thenReturn(false);
            when(categoryRepository.save(any())).thenReturn(rootCategory);

            Category result = categoryService.createCategory(rootCategory);

            assertThat(result.getSlug()).isEqualTo("electronics");
            assertThat(result.isRoot()).isTrue();
            verify(categoryRepository).save(rootCategory);
        }

        @Test
        @DisplayName("should throw when slug already taken")
        void shouldThrowOnDuplicateSlug() {
            when(categoryRepository.existsBySlug("electronics")).thenReturn(true);

            assertThatThrownBy(() -> categoryService.createCategory(rootCategory))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("slug already exists");
        }

        @Test
        @DisplayName("should throw when parent category does not exist")
        void shouldThrowWhenParentMissing() {
            UUID parentId = UUID.randomUUID();
            rootCategory.setParentId(parentId);
            when(categoryRepository.existsBySlug(any())).thenReturn(false);
            when(categoryRepository.findById(parentId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> categoryService.createCategory(rootCategory))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteCategory")
    class DeleteCategory {

        @Test
        @DisplayName("should delete category with no children and no products")
        void shouldDeleteSuccessfully() {
            when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));
            when(categoryRepository.hasChildren(rootId)).thenReturn(false);
            when(categoryRepository.hasProducts(rootId)).thenReturn(false);

            categoryService.deleteCategory(rootId);

            verify(categoryRepository).deleteById(rootId);
        }

        @Test
        @DisplayName("should throw when category has children")
        void shouldThrowWhenHasChildren() {
            when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));
            when(categoryRepository.hasChildren(rootId)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.deleteCategory(rootId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("subcategories");
        }

        @Test
        @DisplayName("should throw when category has products")
        void shouldThrowWhenHasProducts() {
            when(categoryRepository.findById(rootId)).thenReturn(Optional.of(rootCategory));
            when(categoryRepository.hasChildren(rootId)).thenReturn(false);
            when(categoryRepository.hasProducts(rootId)).thenReturn(true);

            assertThatThrownBy(() -> categoryService.deleteCategory(rootId))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("products");
        }
    }
}
