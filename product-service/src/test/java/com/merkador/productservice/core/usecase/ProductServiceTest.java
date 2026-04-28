package com.merkador.productservice.core.usecase;

import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.core.exception.BusinessException;
import com.merkador.productservice.core.exception.InsufficientStockException;
import com.merkador.productservice.core.exception.ResourceNotFoundException;
import com.merkador.productservice.core.port.out.CategoryRepository;
import com.merkador.productservice.core.port.out.EsOutboxRepository;
import com.merkador.productservice.core.port.out.EventPublisher;
import com.merkador.productservice.core.port.out.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock ProductRepository productRepository;
    @Mock CategoryRepository categoryRepository;
    @Mock EsOutboxRepository esOutboxRepository;
    @Mock EventPublisher eventPublisher;

    @InjectMocks ProductService productService;

    private UUID vendorId;
    private UUID productId;
    private UUID categoryId;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        vendorId = UUID.randomUUID();
        productId = UUID.randomUUID();
        categoryId = UUID.randomUUID();

        sampleProduct = Product.builder()
                .id(productId)
                .vendorId(vendorId)
                .categoryId(categoryId)
                .title("Test Product")
                .slug("test-product")
                .price(BigDecimal.valueOf(99.99))
                .currency("RON")
                .stock(10)
                .status(ProductStatus.ACTIVE)
                .avgRating(BigDecimal.ZERO)
                .reviewCount(0)
                .build();
    }

    @Nested
    @DisplayName("createProduct")
    class CreateProduct {

        @Test
        @DisplayName("should create product and enqueue ES outbox entry")
        void shouldCreateProductSuccessfully() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock()));
            when(productRepository.existsBySlug("test-product")).thenReturn(false);
            when(productRepository.save(any())).thenReturn(sampleProduct);

            Product result = productService.createProduct(sampleProduct);

            assertThat(result).isNotNull();
            assertThat(result.getTitle()).isEqualTo("Test Product");
            verify(esOutboxRepository).enqueue(productId, "UPSERT");
            verify(eventPublisher).publishProductCreated(any());
        }

        @Test
        @DisplayName("should throw BusinessException when slug already exists")
        void shouldThrowWhenSlugExists() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.of(mock()));
            when(productRepository.existsBySlug("test-product")).thenReturn(true);

            assertThatThrownBy(() -> productService.createProduct(sampleProduct))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("slug already exists");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when category does not exist")
        void shouldThrowWhenCategoryNotFound() {
            when(categoryRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.createProduct(sampleProduct))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("reserveStock")
    class ReserveStock {

        @Test
        @DisplayName("should reserve stock successfully")
        void shouldReserveStock() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);

            productService.reserveStock(productId, 3);

            assertThat(sampleProduct.getStock()).isEqualTo(7);
            verify(productRepository).save(sampleProduct);
            verify(esOutboxRepository).enqueue(productId, "UPSERT");
        }

        @Test
        @DisplayName("should throw InsufficientStockException when not enough stock")
        void shouldThrowWhenNotEnoughStock() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            assertThatThrownBy(() -> productService.reserveStock(productId, 99))
                    .isInstanceOf(InsufficientStockException.class);
        }
    }

    @Nested
    @DisplayName("releaseStock")
    class ReleaseStock {

        @Test
        @DisplayName("should release stock and add back to inventory")
        void shouldReleaseStock() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);

            productService.releaseStock(productId, 5);

            assertThat(sampleProduct.getStock()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("deleteProduct")
    class DeleteProduct {

        @Test
        @DisplayName("should soft-delete product and enqueue DELETE in outbox")
        void shouldSoftDeleteProduct() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);

            productService.deleteProduct(productId, vendorId);

            assertThat(sampleProduct.getStatus()).isEqualTo(ProductStatus.DELETED);
            verify(esOutboxRepository).enqueue(productId, "DELETE");
            verify(eventPublisher).publishProductDeleted(any());
        }

        @Test
        @DisplayName("should throw BusinessException when vendor does not own product")
        void shouldThrowWhenNotOwner() {
            UUID otherVendor = UUID.randomUUID();
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));

            assertThatThrownBy(() -> productService.deleteProduct(productId, otherVendor))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("does not belong to this vendor");
        }
    }

    @Nested
    @DisplayName("updateRating")
    class UpdateRating {

        @Test
        @DisplayName("should update denormalized rating fields")
        void shouldUpdateRating() {
            when(productRepository.findById(productId)).thenReturn(Optional.of(sampleProduct));
            when(productRepository.save(any())).thenReturn(sampleProduct);

            productService.updateRating(productId, BigDecimal.valueOf(4.5), 42);

            assertThat(sampleProduct.getAvgRating()).isEqualByComparingTo("4.5");
            assertThat(sampleProduct.getReviewCount()).isEqualTo(42);
        }
    }
}
