package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.*;
import com.merkador.productservice.infrastructure.persistence.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PersistenceMapper {

    // ---- Category ----
    CategoryEntity toEntity(Category category);
    Category toDomain(CategoryEntity entity);

    // ---- Product ----
    @Mapping(target = "images", ignore = true)
    ProductEntity toEntity(Product product);

    @Mapping(source = "images", target = "images")
    Product toDomain(ProductEntity entity);

    // ---- ProductImage ----
    @Mapping(target = "product", ignore = true)
    @Mapping(source = "productId", target = "product.id")
    ProductImageEntity toEntity(ProductImage image);

    @Mapping(source = "product.id", target = "productId")
    ProductImage toDomain(ProductImageEntity entity);
}
