package com.merkador.productservice.presentation.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private int sortOrder;
    private boolean isActive;
    private OffsetDateTime createdAt;
}
