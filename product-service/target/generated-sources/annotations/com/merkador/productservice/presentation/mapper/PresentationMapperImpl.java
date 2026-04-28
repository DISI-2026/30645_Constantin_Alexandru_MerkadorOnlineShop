package com.merkador.productservice.presentation.mapper;

import com.merkador.productservice.core.domain.Category;
import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductImage;
import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.core.domain.ProductVariant;
import com.merkador.productservice.presentation.dto.request.CategoryRequest;
import com.merkador.productservice.presentation.dto.request.CreateProductRequest;
import com.merkador.productservice.presentation.dto.request.ProductImageRequest;
import com.merkador.productservice.presentation.dto.request.ProductVariantRequest;
import com.merkador.productservice.presentation.dto.request.UpdateProductRequest;
import com.merkador.productservice.presentation.dto.response.CategoryResponse;
import com.merkador.productservice.presentation.dto.response.ProductImageResponse;
import com.merkador.productservice.presentation.dto.response.ProductResponse;
import com.merkador.productservice.presentation.dto.response.ProductVariantResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-28T12:23:11+0300",
    comments = "version: 1.6.3, compiler: javac, environment: Java 21.0.11 (Amazon.com Inc.)"
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
        product.variants( productVariantRequestListToProductVariantList( request.getVariants() ) );

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
        productResponse.vendorId( product.getVendorId() );
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
        productResponse.variants( productVariantListToProductVariantResponseList( product.getVariants() ) );

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

    @Override
    public ProductVariant toDomain(ProductVariantRequest request) {
        if ( request == null ) {
            return null;
        }

        ProductVariant.ProductVariantBuilder productVariant = ProductVariant.builder();

        productVariant.sku( request.getSku() );
        productVariant.label( request.getLabel() );
        productVariant.priceModifier( request.getPriceModifier() );
        productVariant.stock( request.getStock() );

        return productVariant.build();
    }

    @Override
    public ProductVariantResponse toResponse(ProductVariant variant) {
        if ( variant == null ) {
            return null;
        }

        ProductVariantResponse.ProductVariantResponseBuilder productVariantResponse = ProductVariantResponse.builder();

        productVariantResponse.id( variant.getId() );
        productVariantResponse.sku( variant.getSku() );
        productVariantResponse.label( variant.getLabel() );
        productVariantResponse.priceModifier( variant.getPriceModifier() );
        productVariantResponse.stock( variant.getStock() );

        return productVariantResponse.build();
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

    protected List<ProductVariant> productVariantRequestListToProductVariantList(List<ProductVariantRequest> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductVariant> list1 = new ArrayList<ProductVariant>( list.size() );
        for ( ProductVariantRequest productVariantRequest : list ) {
            list1.add( toDomain( productVariantRequest ) );
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

    protected List<ProductVariantResponse> productVariantListToProductVariantResponseList(List<ProductVariant> list) {
        if ( list == null ) {
            return null;
        }

        List<ProductVariantResponse> list1 = new ArrayList<ProductVariantResponse>( list.size() );
        for ( ProductVariant productVariant : list ) {
            list1.add( toResponse( productVariant ) );
        }

        return list1;
    }
}
