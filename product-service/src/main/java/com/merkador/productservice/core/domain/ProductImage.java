package com.merkador.productservice.core.domain;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {

    private UUID id;
    private UUID productId;
    private String url;
    private String altText;
    private int sortOrder;
}


