package com.merkador.productservice.infrastructure.persistence.adapter;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.infrastructure.persistence.entity.CategoryEntity;
import com.merkador.productservice.infrastructure.persistence.entity.ProductEntity;
import com.merkador.productservice.infrastructure.persistence.entity.ProductImageEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-06T19:55:55+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PersistenceMapperImpl implements PersistenceMapper {

    @Override
    public CategoryEntity toEntity(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryEntity.CategoryEntityBuilder categoryEntity = CategoryEntity.builder();

        categoryEntity.id( category.getId() );
        categoryEntity.name( category.getName() );
        categoryEntity.slug( category.getSlug() );
        categoryEntity.parentId( category.getParentId() );
        categoryEntity.sortOrder( category.getSortOrder() );
        categoryEntity.createdAt( category.getCreatedAt() );

        return categoryEntity.build();
    }

    @Override
    public Category toDomain(CategoryEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.id( entity.getId() );
        category.name( entity.getName() );
        category.slug( entity.getSlug() );
        category.parentId( entity.getParentId() );
        category.sortOrder( entity.getSortOrder() );
        category.createdAt( entity.getCreatedAt() );

        return category.build();
    }

    @Override
    public ProductEntity toEntity(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductEntity.ProductEntityBuilder productEntity = ProductEntity.builder();

        productEntity.id( product.getId() );
        productEntity.sellerId( product.getSellerId() );
        productEntity.categoryId( product.getCategoryId() );
        productEntity.title( product.getTitle() );
        productEntity.slug( product.getSlug() );
        productEntity.description( product.getDescription() );
        productEntity.price( product.getPrice() );
        productEntity.currency( product.getCurrency() );
        productEntity.stock( product.getStock() );
        productEntity.status( product.getStatus() );
        productEntity.avgRating( product.getAvgRating() );
        productEntity.reviewCount( product.getReviewCount() );
        productEntity.createdAt( product.getCreatedAt() );
        productEntity.updatedAt( product.getUpdatedAt() );

        return productEntity.build();
    }

    @Override
    public Product toDomain(ProductEntity entity) {
        if ( entity == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.images( productImageEntityListToProductImageList( entity.getImages() ) );
        product.id( entity.getId() );
        product.sellerId( entity.getSellerId() );
        product.categoryId( entity.getCategoryId() );
        product.title( entity.getTitle() );
        product.slug( entity.getSlug() );
        product.description( entity.getDescription() );
        product.price( entity.getPrice() );
        product.currency( entity.getCurrency() );
        product.stock( entity.getStock() );
        product.status( entity.getStatus() );
        product.avgRating( entity.getAvgRating() );
        product.reviewCount( entity.getReviewCount() );
        product.createdAt( entity.getCreatedAt() );
        product.updatedAt( entity.getUpdatedAt() );

        return product.build();
    }

    @Override
    public ProductImageEntity toEntity(ProductImage image) {
        if ( image == null ) {
            return null;
        }

        ProductImageEntity.ProductImageEntityBuilder productImageEntity = ProductImageEntity.builder();

        productImageEntity.product( productImageToProductEntity( image ) );
        productImageEntity.id( image.getId() );
        productImageEntity.url( image.getUrl() );
        productImageEntity.altText( image.getAltText() );
        productImageEntity.sortOrder( image.getSortOrder() );

        return productImageEntity.build();
    }

    @Override
    public ProductImage toDomain(ProductImageEntity entity) {
        if ( entity == null ) {
            return null;
        }

        ProductImage.ProductImageBuilder productImage = ProductImage.builder();

        productImage.productId( entityProductId( entity ) );
        productImage.id( entity.getId() );
        productImage.url( entity.getUrl() );
        productImage.altText( entity.getAltText() );
        productImage.sortOrder( entity.getSortOrder() );

        return productImage.build();
    }

    protected List<ProductImage> productImageEntityListToProductImageList(List<ProductImageEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductImage> list1 = new ArrayList<ProductImage>( list.size() );
        for ( ProductImageEntity productImageEntity : list ) {
            list1.add( toDomain( productImageEntity ) );
        }

        return list1;
    }

    protected ProductEntity productImageToProductEntity(ProductImage productImage) {
        if ( productImage == null ) {
            return null;
        }

        ProductEntity.ProductEntityBuilder productEntity = ProductEntity.builder();

        productEntity.id( productImage.getProductId() );

        return productEntity.build();
    }

    private UUID entityProductId(ProductImageEntity productImageEntity) {
        ProductEntity product = productImageEntity.getProduct();
        if ( product == null ) {
            return null;
        }
        return product.getId();
    }
}
