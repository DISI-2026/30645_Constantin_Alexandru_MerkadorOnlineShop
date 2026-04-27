package com.merkador.productservice.presentation.controller;

import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.port.in.ProductImageUseCase;
import com.merkador.productservice.infrastructure.security.AuthenticatedUser;
import com.merkador.productservice.presentation.dto.request.ProductImageRequest;
import com.merkador.productservice.presentation.dto.request.ReorderImagesRequest;
import com.merkador.productservice.presentation.dto.response.ApiResponse;
import com.merkador.productservice.presentation.dto.response.ProductImageResponse;
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
@RequestMapping("/api/v1/products/{productId}/images")
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageUseCase imageUseCase;
    private final PresentationMapper mapper;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImages(
            @PathVariable UUID productId) {
        List<ProductImageResponse> images = imageUseCase.getImagesForProduct(productId)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(images));
    }

    @PostMapping
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImage(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductImageRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ProductImage saved = imageUseCase.addImage(productId, mapper.toDomain(request), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved)));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        imageUseCase.deleteImage(productId, imageId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Image deleted"));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable UUID productId,
            @Valid @RequestBody ReorderImagesRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        imageUseCase.reorderImages(productId, request.getOrderedImageIds(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Images reordered"));
    }
}
