package com.merkador.productservice.presentation.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ReorderImagesRequest {
    @NotEmpty
    private List<UUID> orderedImageIds;
}


