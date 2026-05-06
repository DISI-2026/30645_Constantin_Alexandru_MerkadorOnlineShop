package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record StockUpdatedEvent(UUID productId, UUID sellerId, int newStock) {}


