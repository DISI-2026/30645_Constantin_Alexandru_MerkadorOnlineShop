package com.merkador.productservice.presentation.controller;

import com.merkador.productservice.core.domain.ProductVariant;
import com.merkador.productservice.core.port.in.ProductVariantUseCase;
import com.merkador.productservice.infrastructure.security.AuthenticatedUser;
import com.merkador.productservice.presentation.dto.request.ProductVariantRequest;
import com.merkador.productservice.presentation.dto.response.ApiResponse;
import com.merkador.productservice.presentation.dto.response.ProductVariantResponse;
import com.merkador.productservice.presentation.mapper.PresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products/{productId}/variants")
@RequiredArgsConstructor
public class ProductVariantController {

    private final ProductVariantUseCase variantUseCase;
    private final PresentationMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductVariantResponse>>> getVariants(
            @PathVariable UUID productId) {
        List<ProductVariantResponse> variants = variantUseCase.getVariantsForProduct(productId)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(variants));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> addVariant(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductVariantRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ProductVariant saved = variantUseCase.addVariant(productId, mapper.toDomain(request), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved)));
    }

    @PutMapping("/{variantId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductVariantResponse>> updateVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @Valid @RequestBody ProductVariantRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ProductVariant updated = variantUseCase.updateVariant(
                productId, variantId, mapper.toDomain(request), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    @DeleteMapping("/{variantId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteVariant(
            @PathVariable UUID productId,
            @PathVariable UUID variantId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        variantUseCase.deleteVariant(productId, variantId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Variant deleted"));
    }
}
