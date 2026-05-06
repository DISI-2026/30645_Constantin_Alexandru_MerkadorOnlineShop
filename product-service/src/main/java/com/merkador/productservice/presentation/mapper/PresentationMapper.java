package com.merkador.productservice.presentation.mapper;

import com.merkador.productservice.core.domain.*;
import com.merkador.productservice.presentation.dto.request.*;
import com.merkador.productservice.presentation.dto.response.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PresentationMapper {

    // ---- Category ----
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Category toDomain(CategoryRequest request);

    CategoryResponse toResponse(Category category);

    // ---- Product ----
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "avgRating", constant = "0.0")
    @Mapping(target = "reviewCount", constant = "0")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Product toDomain(CreateProductRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sellerId", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "avgRating", ignore = true)
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "images", ignore = true)
    Product toDomain(UpdateProductRequest request);

    ProductResponse toResponse(Product product);

    // ---- ProductImage ----
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productId", ignore = true)
    ProductImage toDomain(ProductImageRequest request);

    ProductImageResponse toResponse(ProductImage image);
}
