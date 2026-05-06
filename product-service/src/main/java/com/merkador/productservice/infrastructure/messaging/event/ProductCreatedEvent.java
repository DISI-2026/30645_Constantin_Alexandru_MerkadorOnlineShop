package com.merkador.productservice.infrastructure.messaging.event;

import java.util.UUID;

public record ProductCreatedEvent(UUID productId, UUID sellerId, String title) {}


