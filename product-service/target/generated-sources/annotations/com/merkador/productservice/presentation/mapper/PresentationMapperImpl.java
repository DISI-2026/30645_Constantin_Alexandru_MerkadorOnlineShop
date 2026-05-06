package com.merkador.productservice.presentation.mapper;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.presentation.dto.request.CategoryRequest;
import com.merkador.productservice.presentation.dto.request.CreateProductRequest;
import com.merkador.productservice.presentation.dto.request.ProductImageRequest;
import com.merkador.productservice.presentation.dto.request.UpdateProductRequest;
import com.merkador.productservice.presentation.dto.response.CategoryResponse;
import com.merkador.productservice.presentation.dto.response.ProductImageResponse;
import com.merkador.productservice.presentation.dto.response.ProductResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-06T19:55:55+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class PresentationMapperImpl implements PresentationMapper {

    @Override
    public Category toDomain(CategoryRequest request) {
        if ( request == null ) {
            return null;
        }

        Category.CategoryBuilder category = Category.builder();

        category.name( request.getName() );
        category.slug( request.getSlug() );
        category.parentId( request.getParentId() );
        category.sortOrder( request.getSortOrder() );

        return category.build();
    }

    @Override
    public CategoryResponse toResponse(Category category) {
        if ( category == null ) {
            return null;
        }

        CategoryResponse.CategoryResponseBuilder categoryResponse = CategoryResponse.builder();

        categoryResponse.id( category.getId() );
        categoryResponse.name( category.getName() );
        categoryResponse.slug( category.getSlug() );
        categoryResponse.parentId( category.getParentId() );
        categoryResponse.sortOrder( category.getSortOrder() );
        categoryResponse.createdAt( category.getCreatedAt() );

        return categoryResponse.build();
    }

    @Override
    public Product toDomain(CreateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.categoryId( request.getCategoryId() );
        product.title( request.getTitle() );
        product.slug( request.getSlug() );
        product.description( request.getDescription() );
        product.price( request.getPrice() );
        product.currency( request.getCurrency() );
        product.stock( request.getStock() );
        product.images( productImageRequestListToProductImageList( request.getImages() ) );

        product.status( ProductStatus.DRAFT );
        product.avgRating( new BigDecimal( "0.0" ) );
        product.reviewCount( 0 );

        return product.build();
    }

    @Override
    public Product toDomain(UpdateProductRequest request) {
        if ( request == null ) {
            return null;
        }

        Product.ProductBuilder product = Product.builder();

        product.categoryId( request.getCategoryId() );
        product.title( request.getTitle() );
        product.slug( request.getSlug() );
        product.description( request.getDescription() );
        product.price( request.getPrice() );
        product.currency( request.getCurrency() );
        product.stock( request.getStock() );

        return product.build();
    }

    @Override
    public ProductResponse toResponse(Product product) {
        if ( product == null ) {
            return null;
        }

        ProductResponse.ProductResponseBuilder productResponse = ProductResponse.builder();

        productResponse.id( product.getId() );
        productResponse.sellerId( product.getSellerId() );
        productResponse.categoryId( product.getCategoryId() );
        productResponse.title( product.getTitle() );
        productResponse.slug( product.getSlug() );
        productResponse.description( product.getDescription() );
        productResponse.price( product.getPrice() );
        productResponse.currency( product.getCurrency() );
        productResponse.stock( product.getStock() );
        productResponse.status( product.getStatus() );
        productResponse.avgRating( product.getAvgRating() );
        productResponse.reviewCount( product.getReviewCount() );
        productResponse.createdAt( product.getCreatedAt() );
        productResponse.updatedAt( product.getUpdatedAt() );
        productResponse.images( productImageListToProductImageResponseList( product.getImages() ) );

        return productResponse.build();
    }

    @Override
    public ProductImage toDomain(ProductImageRequest request) {
        if ( request == null ) {
            return null;
        }

        ProductImage.ProductImageBuilder productImage = ProductImage.builder();

        productImage.url( request.getUrl() );
        productImage.altText( request.getAltText() );
        productImage.sortOrder( request.getSortOrder() );

        return productImage.build();
    }

    @Override
    public ProductImageResponse toResponse(ProductImage image) {
        if ( image == null ) {
            return null;
        }

        ProductImageResponse.ProductImageResponseBuilder productImageResponse = ProductImageResponse.builder();

        productImageResponse.id( image.getId() );
        productImageResponse.url( image.getUrl() );
        productImageResponse.altText( image.getAltText() );
        productImageResponse.sortOrder( image.getSortOrder() );

        return productImageResponse.build();
    }

    protected List<ProductImage> productImageRequestListToProductImageList(List<ProductImageRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductImage> list1 = new ArrayList<ProductImage>( list.size() );
        for ( ProductImageRequest productImageRequest : list ) {
            list1.add( toDomain( productImageRequest ) );
        }

        return list1;
    }

    protected List<ProductImageResponse> productImageListToProductImageResponseList(List<ProductImage> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductImageResponse> list1 = new ArrayList<ProductImageResponse>( list.size() );
        for ( ProductImage productImage : list ) {
            list1.add( toResponse( productImage ) );
        }

        return list1;
    }
}
