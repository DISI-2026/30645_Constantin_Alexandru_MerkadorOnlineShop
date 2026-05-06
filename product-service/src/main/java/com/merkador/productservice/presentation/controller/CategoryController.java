package com.merkador.productservice.presentation.controller;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.port.in.CategoryUseCase;
import com.merkador.productservice.presentation.dto.request.CategoryRequest;
import com.merkador.productservice.presentation.dto.response.ApiResponse;
import com.merkador.productservice.presentation.dto.response.CategoryResponse;
import com.merkador.productservice.presentation.mapper.PresentationMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping({"/v1/categories", "/products/v1/categories"})
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryUseCase categoryUseCase;
    private final PresentationMapper mapper;

    // ---- Public endpoints ----

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllActive() {
        List<CategoryResponse> categories = categoryUseCase.getAllActiveCategories()
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(categories));
    }

    @GetMapping("/roots")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getRoots() {
        List<CategoryResponse> roots = categoryUseCase.getRootCategories()
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(roots));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getById(@PathVariable UUID id) {
        CategoryResponse response = mapper.toResponse(categoryUseCase.getCategoryById(id));
        return ResponseEntity.ok(ApiResponse.ok(response));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getChildren(@PathVariable UUID id) {
        List<CategoryResponse> children = categoryUseCase.getChildCategories(id)
                .stream().map(mapper::toResponse).toList();
        return ResponseEntity.ok(ApiResponse.ok(children));
    }

    // ---- Admin endpoints ----

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(
            @Valid @RequestBody CategoryRequest request) {

        Category category = mapper.toDomain(request);
        category.setActive(true);

        Category saved = categoryUseCase.createCategory(category);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(mapper.toResponse(saved), "Category created successfully"));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryRequest request) {
        Category updated = categoryUseCase.updateCategory(id, mapper.toDomain(request));
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(updated)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        categoryUseCase.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.ok(null, "Category deleted successfully"));
    }
}


