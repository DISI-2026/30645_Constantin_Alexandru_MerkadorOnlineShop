package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record OrderStockReserveMessage(UUID productId, int quantity, UUID orderId) {}
