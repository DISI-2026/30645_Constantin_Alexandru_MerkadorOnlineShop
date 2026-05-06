package com.merkador.productservice.core.exception;

import java.util.UUID;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(UUID productId, int requested, int available) {
        super("Insufficient stock for product %s: requested=%d, available=%d"
                .formatted(productId, requested, available));
    }
}


