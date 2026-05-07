package com.merkador.productservice.presentation.controller;

import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.port.in.ProductImageUseCase;
import com.merkador.productservice.infrastructure.security.AuthenticatedUser;
import com.merkador.productservice.infrastructure.storage.LocalFileStorageService;
import com.merkador.productservice.presentation.dto.request.ProductImageRequest;
import com.merkador.productservice.presentation.dto.request.ReorderImagesRequest;
import com.merkador.productservice.presentation.dto.response.ApiResponse;
import com.merkador.productservice.presentation.dto.response.ProductImageResponse;
import com.merkador.productservice.presentation.mapper.PresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({
        "/v1/products/{productId}/images",
        "/products/v1/products/{productId}/images"
})
@RequiredArgsConstructor
public class ProductImageController {

    private final ProductImageUseCase imageUseCase;
    private final PresentationMapper mapper;
    private final LocalFileStorageService fileStorageService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getImages(
            @PathVariable UUID productId) {
        List<ProductImageResponse> images = imageUseCase.getImagesForProduct(productId)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(images));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImageFile(
            @PathVariable UUID productId,
            @RequestPart("file") MultipartFile file,
            @RequestParam(required = false) String altText,
            @RequestParam(defaultValue = "0") int sortOrder,
            @AuthenticationPrincipal AuthenticatedUser user) {

        String imageUrl = fileStorageService.saveProductImage(file);

        ProductImage image = ProductImage.builder()
                .url(imageUrl)
                .altText(altText)
                .sortOrder(sortOrder)
                .build();

        ProductImage saved = imageUseCase.addImage(productId, image, user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductImageResponse>> addImageJson(
            @PathVariable UUID productId,
            @Valid @RequestBody ProductImageRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        ProductImage saved = imageUseCase.addImage(productId, mapper.toDomain(request), user.getUserId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved)));
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID productId,
            @PathVariable UUID imageId,
            @AuthenticationPrincipal AuthenticatedUser user) {

        // Get the image URL to delete from the database
        String imageUrlToDelete = imageUseCase.getImagesForProduct(productId).stream()
                .filter(img -> img.getId().equals(imageId))
                .map(ProductImage::getUrl)
                .findFirst()
                .orElse(null);
        // Delete the image from the file storage
        if (imageUrlToDelete != null) {
            fileStorageService.deleteProductImageByUrl(imageUrlToDelete);
        }
        // Delete the image from the database
        imageUseCase.deleteImage(productId, imageId, user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Image deleted"));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<Void>> reorder(
            @PathVariable UUID productId,
            @Valid @RequestBody ReorderImagesRequest request,
            @AuthenticationPrincipal AuthenticatedUser user) {

        imageUseCase.reorderImages(productId, request.getOrderedImageIds(), user.getUserId());
        return ResponseEntity.ok(ApiResponse.ok(null, "Images reordered"));
    }
}
