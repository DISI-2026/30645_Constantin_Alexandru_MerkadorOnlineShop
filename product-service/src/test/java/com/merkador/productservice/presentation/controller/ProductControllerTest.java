package com.merkador.productservice.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.merkador.productservice.core.domain.Product;
import com.merkador.productservice.core.domain.ProductStatus;
import com.merkador.productservice.core.port.in.ProductUseCase;
import com.merkador.productservice.infrastructure.security.AuthenticatedUser;
import com.merkador.productservice.infrastructure.security.JwtAuthenticationFilter;
import com.merkador.productservice.presentation.dto.request.CreateProductRequest;
import com.merkador.productservice.presentation.mapper.PresentationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = ProductController.class,
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ASSIGNABLE_TYPE,
        classes = JwtAuthenticationFilter.class
    )
)
@DisplayName("ProductController")
class ProductControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;

    @MockBean ProductUseCase productUseCase;
    @MockBean PresentationMapper mapper;

    private UUID sellerId;
    private UUID productId;
    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        productId = UUID.randomUUID();

        sampleProduct = Product.builder()
                .id(productId)
                .sellerId(sellerId)
                .categoryId(UUID.randomUUID())
                .title("Sample Product")
                .slug("sample-product")
                .price(BigDecimal.valueOf(149.99))
                .currency("RON")
                .stock(25)
                .status(ProductStatus.ACTIVE)
                .avgRating(BigDecimal.valueOf(4.2))
                .reviewCount(10)
                .createdAt(OffsetDateTime.now())
                .updatedAt(OffsetDateTime.now())
                .build();
    }

    private void authenticateAsSELLER() {
        AuthenticatedUser principal = new AuthenticatedUser(sellerId, "SELLER");
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_SELLER")));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("GET /api/v1/products/{id} - returns product for public")
    void shouldReturnProductById() throws Exception {
        when(productUseCase.getProductById(productId)).thenReturn(sampleProduct);
        when(mapper.toResponse(sampleProduct)).thenReturn(
                com.merkador.productservice.presentation.dto.response.ProductResponse.builder()
                        .id(productId)
                        .title("Sample Product")
                        .price(BigDecimal.valueOf(149.99))
                        .status(ProductStatus.ACTIVE)
                        .build()
        );

        mockMvc.perform(get("/api/v1/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(productId.toString()))
                .andExpect(jsonPath("$.data.title").value("Sample Product"));
    }

    @Test
    @DisplayName("POST /api/v1/products - creates product for authenticated SELLER")
    void shouldCreateProductAsSELLER() throws Exception {
        authenticateAsSELLER();

        CreateProductRequest request = new CreateProductRequest();
        request.setTitle("New Product");
        request.setSlug("new-product");
        request.setPrice(BigDecimal.valueOf(59.99));
        request.setCurrency("RON");
        request.setStock(10);
        request.setCategoryId(UUID.randomUUID());

        when(mapper.toDomain(any(CreateProductRequest.class))).thenReturn(sampleProduct);
        when(productUseCase.createProduct(any())).thenReturn(sampleProduct);
        when(mapper.toResponse(sampleProduct)).thenReturn(
                com.merkador.productservice.presentation.dto.response.ProductResponse.builder()
                        .id(productId)
                        .title("New Product")
                        .status(ProductStatus.DRAFT)
                        .build()
        );

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("POST /api/v1/products - returns 400 when request is invalid")
    void shouldReturn400OnInvalidRequest() throws Exception {
        authenticateAsSELLER();

        CreateProductRequest invalid = new CreateProductRequest();
        // title is blank, price is null → validation should fail

        mockMvc.perform(post("/api/v1/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
