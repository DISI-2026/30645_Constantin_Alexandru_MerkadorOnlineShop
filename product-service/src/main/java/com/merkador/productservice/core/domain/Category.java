package com.merkador.productservice.core.domain;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private UUID id;
    private String name;
    private String slug;
    private UUID parentId;
    private int sortOrder;
    private boolean isActive;
    private OffsetDateTime createdAt;

    public boolean isRoot() {
        return parentId == null;
    }
}
