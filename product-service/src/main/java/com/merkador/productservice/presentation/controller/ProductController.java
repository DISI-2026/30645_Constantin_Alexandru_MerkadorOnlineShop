package com.merkador.productservice.presentation.controller;

import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.core.port.in.ProductFilter;
import com.merkador.productservice.core.port.in.ProductUseCase;
import com.merkador.productservice.infrastructure.security.AuthenticatedUser;
import com.merkador.productservice.presentation.dto.request.CreateProductRequest;
import com.merkador.productservice.presentation.dto.request.UpdatePriceRequest;
import com.merkador.productservice.presentation.dto.request.UpdateProductRequest;
import com.merkador.productservice.presentation.dto.request.UpdateStockRequest;
import com.merkador.productservice.presentation.dto.response.ApiResponse;
import com.merkador.productservice.presentation.dto.response.PageResponse;
import com.merkador.productservice.presentation.dto.response.ProductResponse;
import com.merkador.productservice.presentation.mapper.PresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping({"/v1/products", "/products/v1/products"})
@RequiredArgsConstructor
public class ProductController {

    private final ProductUseCase productUseCase;
    private final PresentationMapper mapper;

    // ================================================================
    // PUBLIC ENDPOINTS
    // ================================================================

    /**
     * Search & filter products (public).
     * GET /api/v1/products?categoryId=&minPrice=&maxPrice=&minRating=&search=&page=&size=&sortBy=&sortDir=
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> search(
            @RequestParam(required = false) UUID categoryId,
            @RequestParam(required = false) UUID sellerId,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minRating,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        ProductFilter filter = ProductFilter.builder()
                .categoryId(categoryId)
                .sellerId(sellerId)
                .searchTerm(search)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .status(ProductStatus.ACTIVE)   // public always sees only ACTIVE
                .page(page)
                .size(Math.min(size, 100))      // cap at 100
                .sortBy(sortBy)
                .sortDirection(sortDir)
                .build();

        Page<ProductResponse> resultPage = productUseCase.searchProducts(filter)
                .map(mapper::toResponse);

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(resultPage)));
    }

    /**
     * Get single product by ID (public).
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable UUID id) {
        ProductResponse response = mapper.toResponse(productUseCase.getProductById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    // ================================================================
    // SELLER ENDPOINTS
    // ================================================================

    /**
     * SELLER: list own products (with any status).
     */
    @GetMapping("/my")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<PageResponse<ProductResponse>>> getMyProducts(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(required = false) ProductStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ProductFilter filter = ProductFilter.builder()
                .sellerId(user.getUserId())
                .status(status)
                .page(page)
                .size(Math.min(size, 100))
                .sortBy("createdAt")
                .sortDirection("desc")
                .build();

        Page<ProductResponse> resultPage = productUseCase.searchProducts(filter)
                .map(mapper::toResponse);

        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(resultPage)));
    }

    /**
     * SELLER: create a new product.
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(
            @Valid @RequestBody CreateProductRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product domain = mapper.toDomain(request);
        domain.setSellerId(user.getUserId());

        Product saved = productUseCase.createProduct(domain);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved), "Product created successfully"));
    }

    /**
     * SELLER: update product details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product updated = productUseCase.updateProduct(id, mapper.toDomain(request), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    /**
     * SELLER: soft-delete a product.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        productUseCase.deleteProduct(id, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Product deleted successfully"));
    }

    /**
     * SELLER: update stock only.
     */
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateStock(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStockRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product updated = productUseCase.updateStock(id, request.getStock(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    /**
     * SELLER: update price only.
     */
    @PatchMapping("/{id}/price")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> updatePrice(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePriceRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product updated = productUseCase.updatePrice(id, request.getPrice(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    /**
     * SELLER: activate product (make it visible).
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> activate(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product updated = productUseCase.activateProduct(id, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    /**
     * SELLER: deactivate product (hide from public).
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivate(
            @PathVariable UUID id,
            @AuthenticationPrincipal AuthenticatedUser user) {

        Product updated = productUseCase.deactivateProduct(id, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }
}


